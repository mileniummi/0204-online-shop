package com.itmo.microservices.demo.orders.logic

import com.itmo.microservices.demo.common.exceptions.EntityViolationException
import com.itmo.microservices.demo.orders.api.model.*
import com.itmo.microservices.demo.payment.api.model.PaymentRecordLog
import com.itmo.microservices.demo.payment.api.model.PaymentSuccessEvent
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.time.Instant
import java.util.UUID

class OrderAggregateState : AggregateState<UUID, OrderAggregate> {
    private lateinit var orderId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
    private var status: OrderStatus = OrderStatus.COLLECTING
    var paymentHistory: MutableList<PaymentRecordLog> = mutableListOf()
    var itemsMap: Map<UUID, Int > = mapOf()
    override fun getId() = orderId

    fun createOrder(itemsMap: Map<UUID, Int>): OrderCreatedEvent {
        return OrderCreatedEvent(
            UUID.randomUUID(),
            System.currentTimeMillis(), OrderStatus.COLLECTING,
            itemsMap,
        )
    }

    fun updateOrder(orderId: UUID, itemsMap: Map<UUID, Int>): OrderUpdatedEvent {
        if (status !== OrderStatus.COLLECTING) {
            throw EntityViolationException("Order can be updated only on status collecting")
        }
        return OrderUpdatedEvent(orderId, itemsMap)
    }

    fun updateOrderDeliverySlot(orderId: UUID, deliverySlot: Instant): OrderDeliverySlotUpdatedEvent {
        if (status !== OrderStatus.COLLECTING) {
            throw EntityViolationException("Order delivery slot can be updated only on status collecting")
        }
        return OrderDeliverySlotUpdatedEvent(orderId, deliverySlot, OrderStatus.BOOKED)
    }

    fun updateOrderAfterBooking(orderId: UUID, status: OrderStatus, bookingId: UUID): OrderUpdateAfterBooking {
        return OrderUpdateAfterBooking(orderId, status, bookingId)
    }

    fun addPayment(paymentSuccessEvent: PaymentSuccessEvent): PaymentSuccessEvent {
        if (status !== OrderStatus.BOOKED) {
            throw EntityViolationException("Order can be paid only on status booked")
        }
        return paymentSuccessEvent
    }

    @StateTransitionFunc
    fun createOrder(event: OrderCreatedEvent) {
        orderId = event.orderId
        itemsMap = event.itemsMap
    }

    @StateTransitionFunc
    fun updateOrder(event: OrderUpdatedEvent) {
        orderId = event.orderId
        val newItemsMap = itemsMap.toMutableMap()
        for (i in event.itemsMap.entries) {
            newItemsMap[i.key] = i.value
        }
        itemsMap = newItemsMap
    }

    @StateTransitionFunc
    fun updateOrderDeliverySlot(event: OrderDeliverySlotUpdatedEvent) {
        status = OrderStatus.BOOKED
        orderId = event.orderId
    }

    @StateTransitionFunc
    fun updateOrderAfterBooking(event: OrderUpdateAfterBooking) {
        status = event.status
        orderId = event.orderId
    }

    @StateTransitionFunc
    fun processOrderPayment(event: PaymentSuccessEvent){
        status = OrderStatus.PAID
        paymentHistory.add(PaymentRecordLog(event.timestamp, event.amount, event.status, event.transactionId))
    }
}