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
import com.itmo.microservices.demo.orders.api.model.OrderCreatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderDeliverySlotUpdatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderUpdatedEvent
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.utils.ConditionAwaiter
import kotlinx.coroutines.sync.Mutex
import com.itmo.microservices.demo.orders.projections.OrderRepository
import com.itmo.microservices.demo.payment.api.model.PaymentStatus
import com.itmo.microservices.demo.payment.api.model.PaymentSuccessEvent
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
    private val orderRepository: OrderRepository,
    private val tpsExternalService: TpsExternalService,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {

    fun createOrder(order: CreateOrderRequest?): ResponseEntity<OrderResponse> {
        println("Create new order")
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

    fun setOrderDeliverySlot(slot: Int, orderId: UUID): ResponseEntity<Unit> {
        // update order
        println("Set slot = $slot")
        val order = orderEsService.getState(orderId) ?: throw NotFoundException("Order does not exist.")
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
                orderEsService.update(orderId) {
                        orderState -> orderState.updateOrderDeliverySlot(orderId, delivery.id, slot)
                }
            }
        }
            .onFailure {
                throw ExternalServiceException(it?.message)
            }.startWaiting()
    }
}