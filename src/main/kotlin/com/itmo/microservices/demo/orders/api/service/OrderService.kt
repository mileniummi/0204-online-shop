package com.itmo.microservices.demo.orders.api.service

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.api.service.CatalogItemService
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.common.exceptions.BadRequestException
import com.itmo.microservices.demo.common.exceptions.EntityViolationException
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.external.TpsExternalService
import com.itmo.microservices.demo.external.callbacks.dto.CallbackStatus
import com.itmo.microservices.demo.external.callbacks.logic.ExternalServiceCallbackAggregateState
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackAggregate
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackCreatedEvent
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderCreatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderDeliverySlotUpdatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderUpdatedEvent
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.orders.projections.OrderCache
import com.itmo.microservices.demo.orders.projections.OrderRepository
import com.itmo.microservices.demo.payment.api.model.PaymentSuccessEvent
import com.itmo.microservices.demo.utils.ConditionAwaiter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.streams.AggregateSubscriptionsManager
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class OrderService(
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
    private val orderRepository: OrderRepository,
    private val tpsExternalService: TpsExternalService,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {

    fun createOrder(order: CreateOrderRequest): ResponseEntity<OrderCreatedEvent> {
        if (order.itemsMap.isEmpty()) {
            throw BadRequestException("Attempt to create order with no items.")
        }
        for (orderCatalogItem in order.itemsMap) {
            if (orderCatalogItem.value == 0) {
                throw BadRequestException("Attempt to create order with 0 items.")
            }
            val catalogItem = catalogEsService.getState(orderCatalogItem.key)
                ?: throw NotFoundException("Order item does not exist.")
            if (orderCatalogItem.value > catalogItem.amount) {
                throw EntityViolationException("Order item amount is greater than existing item amount in warehouse.")
            }
        }
        return ResponseEntity(
            orderEsService.create { orderState: OrderAggregateState -> orderState.createOrder(order) },
            HttpStatus.CREATED
        )
    }

    fun addProductToOrder(amount: Int, order_id: UUID, item_id: UUID): ResponseEntity<OrderUpdatedEvent> {
        orderEsService.getState(order_id) ?: throw NotFoundException("Order does not exist.")
        val catalogItem = catalogEsService.getState(item_id) ?: throw NotFoundException("Item does not exist.")
        if (amount == 0) {
            throw EntityViolationException("Attempt to update order with 0 items.")
        }
        if (catalogItem.amount < amount) {
            throw EntityViolationException("Order item amount is greater than existing item amount in warehouse.")
        }
        return ResponseEntity(
            orderEsService.create { orderState -> orderState.updateOrder(order_id, mapOf(item_id to amount)) },
            HttpStatus.OK
        )
    }

    suspend fun setOrderDeliverySlot(slot: Instant, order_id: UUID): ResponseEntity<OrderDeliverySlotUpdatedEvent> {
        val response = tpsExternalService.createDelivery(order_id.toString())
        var resultStatus = CallbackStatus.PENDING
        val mutex = Mutex(true)
        subscriptionsManager.createSubscriber(
            ExternalServiceCallbackAggregate::class, "::callback-subscriber-${response.id}"
        ) {
            `when`(ExternalServiceCallbackCreatedEvent::class) { event ->
                if (event.transactionId == response.id) {
                    resultStatus = event.status
                    mutex.unlock()
                }
            }
        }
        ConditionAwaiter.awaitAtMost(10, TimeUnit.SECONDS).condition {
            mutex.tryLock()
        }.onSuccess {
            println("uraaaaaaa")
            println(resultStatus)
        }.onFailure { println("failure") }.startWaiting()
        val existingOrderWithThisSlot = orderRepository.findByDeliverySlot(slot, slot.plusSeconds(20))
        if (existingOrderWithThisSlot.isPresent) {
            throw BadRequestException("Delivery slot is already taken.")
        }
        // update order
        val order = orderEsService.getState(order_id) ?: throw NotFoundException("Order does not exist.")
        subscriptionsManager.destroy()
        return ResponseEntity(
            orderEsService.update(order.getId()) { orderState -> orderState.updateOrderDeliverySlot(order_id, slot) },
            HttpStatus.OK
        )
    }
}