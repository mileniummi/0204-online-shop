package com.itmo.microservices.demo.orders.api.model

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.UUID

const val ORDER_CREATED = "ORDER_CREATED"
const val ORDER_UPDATED = "ORDER_UPDATED"

@DomainEvent(name = ORDER_CREATED)
data class OrderCreatedEvent(
    val orderId: UUID,
    val timeCreated: Long,
    val status: OrderStatus,
    val itemsMap: Map<UUID, Int>,
) : Event<OrderAggregate>(name = ORDER_CREATED)

@DomainEvent(name = ORDER_UPDATED)
data class OrderUpdatedEvent(
    val orderId: UUID,
    val itemsMap: Map<UUID, Int>,
) : Event<OrderAggregate>(name = ORDER_UPDATED)