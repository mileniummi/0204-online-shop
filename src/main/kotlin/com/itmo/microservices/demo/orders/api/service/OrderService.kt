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
    private val tpsExternalService: TpsExternalService,
    private val subscriptionsManager: AggregateSubscriptionsManager,
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

    suspend fun setOrderDeliverySlot(slot: Instant, order_id: UUID): ResponseEntity<OrderAggregateState> {
        val order = orderEsService.getState(order_id) ?: throw NotFoundException("Order does not exist.")
        val response = tpsExternalService.createDelivery(order_id.toString())
        var status = CallbackStatus.PENDING
        val mutex = Mutex(true)
        subscriptionsManager.createSubscriber(
            ExternalServiceCallbackAggregate::class, "::callback-subscriber-${response.id}"
        ) {
            `when`(ExternalServiceCallbackCreatedEvent::class) { event ->
                if (event.transactionId == response.id) {
                    status = event.status
                    mutex.unlock()
                }
            }
        }
        ConditionAwaiter.awaitAtMost(10, TimeUnit.SECONDS).condition {
            mutex.tryLock()
        }.onSuccess {
            if (status === CallbackStatus.FAILURE) {
                throw ExternalServiceException("Can't set order delivery slot. Request denied by external service.")
            }
            if (status == CallbackStatus.SUCCESS) {
                orderEsService.update(order.getId()) { orderState ->
                    orderState.updateOrderDeliverySlot(
                        order_id,
                        slot
                    )
                }
            }
        }.onFailure {
            throw ExternalServiceException(it?.message)
        }.startWaiting()

        subscriptionsManager.destroy()
        return ResponseEntity(order, HttpStatus.OK)

    }
}