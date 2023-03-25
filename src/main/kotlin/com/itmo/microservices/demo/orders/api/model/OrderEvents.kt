package com.itmo.microservices.demo.orders.api.model

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.time.Instant
import java.util.UUID

const val ORDER_CREATED = "ORDER_CREATED"
const val ORDER_UPDATED = "ORDER_UPDATED"
const val ORDER_COMPLETED = "ORDER_COMPLETED"
const val ORDER_DELIVERY_SLOT_UPDATED = "ORDER_DELIVERY_SLOT_UPDATED"
const val ORDER_UPDATE_AFTER_BOOKING = "ORDER_UPDATE_AFTER_BOOKING"

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

@DomainEvent(name = ORDER_DELIVERY_SLOT_UPDATED)
data class OrderDeliverySlotUpdatedEvent(
    val orderId: UUID,
    val transactionId: UUID,
    val deliverySlot: Int,
    val status: OrderStatus,
) : Event<OrderAggregate>(name = ORDER_DELIVERY_SLOT_UPDATED)

@DomainEvent(name = ORDER_UPDATE_AFTER_BOOKING)
data class OrderUpdateAfterBooking(
    val orderId: UUID,
    val status: OrderStatus,
    val bookingId: UUID
) : Event<OrderAggregate>(name = ORDER_UPDATE_AFTER_BOOKING)

@DomainEvent(name = ORDER_COMPLETED)
data class OrderCompleted(
    val orderId: UUID
) : Event<OrderAggregate>(name = ORDER_COMPLETED)