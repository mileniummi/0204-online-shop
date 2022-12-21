package com.itmo.microservices.demo.orders.logic

import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.model.*
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.time.Instant
import java.util.UUID

class OrderAggregateState : AggregateState<UUID, OrderAggregate> {
    private lateinit var orderId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    override fun getId() = orderId

    fun createOrder(order: CreateOrderRequest): OrderCreatedEvent {
        return OrderCreatedEvent(
            UUID.randomUUID(),
            System.currentTimeMillis(), OrderStatus.COLLECTING,
            order.itemsMap,
        )
    }

    fun updateOrder(orderId: UUID, itemsMap:  Map<UUID, Int>): OrderUpdatedEvent {
        return OrderUpdatedEvent(orderId, itemsMap)
    }

    fun updateOrderDeliverySlot(orderId: UUID, deliverySlot: Instant): OrderDeliverySlotUpdatedEvent {
        return OrderDeliverySlotUpdatedEvent(orderId, deliverySlot)
    }

    @StateTransitionFunc
    fun createOrder (event: OrderCreatedEvent){
        orderId = event.orderId
    }
    @StateTransitionFunc
    fun updateOrder (event: OrderUpdatedEvent){
        orderId = event.orderId
    }

    @StateTransitionFunc
    fun updateOrderDeliverySlot(event: OrderDeliverySlotUpdatedEvent){
        orderId = event.orderId
    }
}