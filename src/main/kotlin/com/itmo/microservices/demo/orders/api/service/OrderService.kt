package com.itmo.microservices.demo.orders.api.service

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.common.exceptions.BadRequestException
import com.itmo.microservices.demo.common.exceptions.EntityViolationException
import com.itmo.microservices.demo.common.exceptions.ExternalServiceException
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.external.callbacks.dto.CallbackStatus
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackAggregate
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackCreatedEvent
import com.itmo.microservices.demo.external.tps.ResponseDto
import com.itmo.microservices.demo.external.tps.TpsExternalService
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.dto.OrderResponse
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderUpdatedEvent
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.utils.ConditionAwaiter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.runBlocking

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.streams.AggregateSubscriptionsManager
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class OrderService(
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
    private val tpsExternalService: TpsExternalService,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {

    fun createOrder(order: CreateOrderRequest?): ResponseEntity<OrderResponse> {
        var orderItems = mutableMapOf<UUID, Int>()
        if (order?.itemsMap != null) {
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
            orderItems = order.itemsMap as MutableMap<UUID, Int>
        }
        val newOrder = orderEsService.create { orderState: OrderAggregateState -> orderState.createOrder(orderItems) }
        return ResponseEntity(
            OrderResponse(
                id = newOrder.orderId,
                timeCreated = newOrder.timeCreated,
                itemsMap = newOrder.itemsMap,
                deliveryDuration = null,
                paymentHistory = listOf(),
            ),
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
            orderEsService.update(order_id) { orderState ->
                orderState.updateOrder(
                    order_id,
                    mapOf(item_id to amount)
                )
            },
            HttpStatus.OK
        )
    }

    fun setOrderDeliverySlot(slot: Int, orderId: UUID): ResponseEntity<Unit> {
        orderEsService.getState(orderId) ?: throw NotFoundException("Order does not exist.")
        val tpsResp = runBlocking { tpsExternalService.createDelivery("Super address") }
        runBlocking { waitForDelivery(tpsResp, orderId, slot) }

        return ResponseEntity(
            Unit,
            HttpStatus.OK
        )
    }

    private suspend fun waitForDelivery(delivery: ResponseDto, orderId: UUID, slot: Int) {
        val mutex = Mutex(true)
        var status = CallbackStatus.PENDING
        subscriptionsManager.createSubscriber(
            ExternalServiceCallbackAggregate::class, "::callback-subscriber-${delivery.id}"
        ) {
            `when`(ExternalServiceCallbackCreatedEvent::class) { event ->
                println("Hello from delivery callback 1 $event")
                if (event.transactionId == delivery.id) {
                    println("Success hello from delivery callback 1 $status")
                    status = event.status
                    mutex.unlock()
                }
            }
        }
        ConditionAwaiter.awaitAtMost(10, TimeUnit.SECONDS).condition {
            mutex.tryLock()
        }.onSuccess {
            if (status !== CallbackStatus.PENDING) {
                println("Success hello from delivery callback 2 $status")
                orderEsService.update(orderId) { orderState ->
                    orderState.updateOrderDeliverySlot(orderId, delivery.id, slot)
                }
            }
        }
            .onFailure {
                throw ExternalServiceException(it?.message)
            }.startWaiting()
    }
}