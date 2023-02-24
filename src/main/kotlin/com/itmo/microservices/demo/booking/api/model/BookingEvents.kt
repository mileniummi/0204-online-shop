package com.itmo.microservices.demo.booking.api.model

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val BOOKING_CREATED = "BOOKING_CREATED"
const val BOOKING_LOG_CREATED = "BOOKING_LOG_CREATED"

@DomainEvent(name = BOOKING_CREATED)
data class BookingCreatedEvent(
    val bookingId: UUID,
    val bookingLogs: Set<UUID>,
) : Event<BookingAggregate>(name = BOOKING_CREATED)

@DomainEvent(name = BOOKING_LOG_CREATED)
data class BookingLogCreatedEvent(
    val bookingLogId: UUID,
    val itemId: UUID,
    val status: BookingStatus,
    val amount: Int,
    val timestamp: Long
) : Event<BookingAggregate>(name = BOOKING_LOG_CREATED)
