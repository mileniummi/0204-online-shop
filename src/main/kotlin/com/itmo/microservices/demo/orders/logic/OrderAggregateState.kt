package com.itmo.microservices.demo.orders.logic

import com.itmo.microservices.demo.common.exceptions.EntityViolationException
import com.itmo.microservices.demo.delivery.api.model.DeliveryLog
import com.itmo.microservices.demo.delivery.api.model.DeliverySubmissionOutcome
import com.itmo.microservices.demo.orders.api.model.*
import com.itmo.microservices.demo.payment.api.model.PaymentRecordLog
import com.itmo.microservices.demo.payment.api.model.PaymentSuccessEvent
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.UUID

class OrderAggregateState : AggregateState<UUID, OrderAggregate> {
    private lateinit var orderId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
    private var status: OrderStatus = OrderStatus.COLLECTING
    var deliverySlot: Int? = null
    var paymentHistory: MutableList<PaymentRecordLog> = mutableListOf()
    var deliveryHistory: MutableList<DeliveryLog> = mutableListOf()
    var itemsMap: Map<UUID, Int > = mapOf()
    override fun getId() = orderId
    fun getStatus() = status

    fun createOrder(itemsMap: Map<UUID, Int>): OrderCreatedEvent {
        return OrderCreatedEvent(
            UUID.randomUUID(),
            System.currentTimeMillis(), OrderStatus.COLLECTING,
            itemsMap,
        )
    }

    fun updateOrder(orderId: UUID, itemsMap: Map<UUID, Int>): OrderUpdatedEvent {
        if (status !== OrderStatus.COLLECTING && status !== OrderStatus.BOOKED && status !== OrderStatus.SHIPPING) {
            throw EntityViolationException("Order can be updated only on status collecting")
        }
        return OrderUpdatedEvent(orderId, itemsMap)
    }

    fun updateOrderDeliverySlot(orderId: UUID, transactionId: UUID, deliverySlot: Int): OrderDeliverySlotUpdatedEvent {
        if (status !== OrderStatus.BOOKED && status !== OrderStatus.SHIPPING) {
            throw EntityViolationException("Order delivery slot can be updated only on status booked. Current $status")
        }
        return OrderDeliverySlotUpdatedEvent(orderId, transactionId, deliverySlot, OrderStatus.SHIPPING)
    }

    fun updateOrderAfterBooking(orderId: UUID, status: OrderStatus, bookingId: UUID): OrderUpdateAfterBooking {
        return OrderUpdateAfterBooking(orderId, status, bookingId)
    }

    fun addPayment(paymentSuccessEvent: PaymentSuccessEvent): PaymentSuccessEvent {
        if (status !== OrderStatus.BOOKED && status !== OrderStatus.SHIPPING) {
            throw EntityViolationException("Order can be paid only on status booked. Current $status")
        }
        return paymentSuccessEvent
    }

    fun completeOrder(orderId: UUID): OrderCompleted {
        return OrderCompleted(orderId)
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
        status = OrderStatus.COLLECTING
        itemsMap = newItemsMap
    }

    @StateTransitionFunc
    fun updateOrderDeliverySlot(event: OrderDeliverySlotUpdatedEvent) {
        status = event.status
        orderId = event.orderId
        deliverySlot = event.deliverySlot
        deliveryHistory.add(
            DeliveryLog(
            outcome = DeliverySubmissionOutcome.SUCCESS,
            preparedTime = System.currentTimeMillis(),
            attempts = 1,
            submittedTime = System.currentTimeMillis(),
            transactionId = event.transactionId,
            submissionStartedTime = System.currentTimeMillis(),
        ))
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

    @StateTransitionFunc
    fun completeOrder(event: OrderCompleted){
        status = OrderStatus.COMPLETED
    }
}