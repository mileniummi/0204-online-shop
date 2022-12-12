package com.itmo.microservices.demo.orders.logic

import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderCreatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
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

    @StateTransitionFunc
    fun createOrder (event: OrderCreatedEvent){
        orderId = event.orderId
        updatedAt = createdAt
    }
}