package com.itmo.microservices.demo.orders.api.service

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.api.service.CatalogItemService
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.common.exceptions.BadRequestException
import com.itmo.microservices.demo.common.exceptions.EntityViolationException
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.dto.OrderResponse
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderDeliverySlotUpdatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderUpdatedEvent
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.orders.projections.OrderRepository

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.time.Instant
import java.util.*

@Service
class OrderService(
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
    private val catalogItemService: CatalogItemService,
    private val orderRepository: OrderRepository
) {

    fun createOrder(order: CreateOrderRequest?): ResponseEntity<OrderResponse> {
        println("Create new order")
//        if(order?.itemsMap != null) {
//            for (orderCatalogItem in order.itemsMap) {
//                if (orderCatalogItem.value == 0) {
//                    throw BadRequestException("Attempt to create order with 0 items.")
//                }
//                val catalogItem = catalogEsService.getState(orderCatalogItem.key)
//                    ?: throw NotFoundException("Order item does not exist.")
//                if (orderCatalogItem.value > catalogItem.amount) {
//                    throw EntityViolationException("Order item amount is greater than existing item amount in warehouse.")
//                }
//            }
//        }
        val o = orderEsService.create { orderState: OrderAggregateState -> orderState.createOrder(mapOf()) }
        println(o)
        val resp = OrderResponse(
            id = o.orderId,
            timeCreated = o.timeCreated,
            itemsMap = mapOf(),
            deliveryDuration = null,
            paymentHistory = listOf(),
        )
        return ResponseEntity(
            resp,
            HttpStatus.CREATED
        )
    }

    fun addProductToOrder(amount: Int, order_id: UUID, item_id: UUID): ResponseEntity<OrderUpdatedEvent> {
        println(order_id)
        println(item_id)
        orderEsService.getState(order_id) ?: throw NotFoundException("Order does not exist.")
        val catalogItem = catalogEsService.getState(item_id) ?: throw NotFoundException("Item does not exist.")
        if (amount == 0) {
            throw EntityViolationException("Attempt to update order with 0 items.")
        }
        if (catalogItem.amount < amount) {
            throw EntityViolationException("Order item amount is greater than existing item amount in warehouse.")
        }
        val ev = orderEsService.update(order_id) { orderState -> orderState.updateOrder(order_id, mapOf(item_id to amount)) }
        return ResponseEntity(
            ev,
            HttpStatus.OK
        )
    }

    fun setOrderDeliverySlot(slot: Instant, order_id: UUID): ResponseEntity<OrderDeliverySlotUpdatedEvent> {
        // todo remake find
        val existingOrderWithThisSlot = orderRepository.findByDeliverySlot(slot, slot.plusSeconds(20))
        if (existingOrderWithThisSlot.isPresent) {
            throw BadRequestException("Delivery slot is already taken.")
        }
        // update order
        val order = orderEsService.getState(order_id) ?: throw NotFoundException("Order does not exist.")
        return ResponseEntity(
            orderEsService.update(order.getId()) { orderState -> orderState.updateOrderDeliverySlot(order_id, slot) },
            HttpStatus.OK
        )
    }
}