package com.itmo.microservices.demo.orders.api.service

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.api.service.CatalogItemService
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.orders.projections.OrderRepository

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Service
class OrderService(
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
    private val catalogItemService: CatalogItemService,
    private val orderRepository: OrderRepository
) {

    fun createOrder(order: CreateOrderRequest): ResponseEntity<Any> {
        for (orderCatalogItem in order.itemsMap) {
            if (orderCatalogItem.value == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attempt to create order with 0 items.")
            }
            val catalogItem = catalogEsService.getState(orderCatalogItem.key)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order item does not exist.")
            if (orderCatalogItem.value > catalogItem.amount) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Order item amount is greater than existing item amount in warehouse.")
            }
        }
        return ResponseEntity(
            orderEsService.create { orderState: OrderAggregateState -> orderState.createOrder(order) },
            HttpStatus.CREATED
        )
    }

    fun addProductToOrder(amount: Int, order_id: UUID, item_id: UUID): ResponseEntity<out Any> {
        orderEsService.getState(order_id) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Order does not exist.")
        val catalogItem = catalogEsService.getState(item_id) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Item does not exist.")
        if (amount == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attempt to update order with 0 items.")
        }
        if (catalogItem.amount < amount) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Order item amount is greater than existing item amount in warehouse.")
        }
        return ResponseEntity(
            orderEsService.create { orderState -> orderState.updateOrder(order_id, mapOf(item_id to amount)) },
            HttpStatus.OK
        )
    }

    fun setOrderDeliverySlot(slot: Instant, order_id: UUID): ResponseEntity<out Any> {
        // todo remake find
        val existingOrderWithThisSlot = orderRepository.findByDeliverySlot(slot, slot.plusSeconds(20))
        if (existingOrderWithThisSlot.isPresent){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Delivery slot is already taken.")
        }
        // update order
        val order = orderEsService.getState(order_id) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Order does not exist.")
        return ResponseEntity(
            orderEsService.update(order.getId()) { orderState -> orderState.updateOrderDeliverySlot(order_id, slot) },
            HttpStatus.OK
        )
    }

    fun processOrderPayment(order_id: UUID): ResponseEntity<out Any> {
        val order = orderEsService.getState(order_id) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Order does not exist.")

        val items = order.itemsMap
        var totalPrice = BigDecimal(0)
        for (item in items){
            val catalogItem = catalogItemService.getCatalogItemById(item.key)
            if (catalogItem.isEmpty) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order item does not exist.")
            }
            totalPrice += catalogItem.get().product.price * BigDecimal(item.value)
        }
        for (orderCatalogItem in order.itemsMap) {
            catalogEsService.update(orderCatalogItem.key) { catalogState ->
                catalogState.updateCatalogItemAmount(
                    orderCatalogItem.key,
                    orderCatalogItem.value, false
                )
            }
        }
        return ResponseEntity(
            orderEsService.update(order.getId()) { orderState -> orderState.processOrderPayment(order_id, totalPrice) },
            HttpStatus.OK
        )
    }
}