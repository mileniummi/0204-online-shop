package com.itmo.microservices.demo.booking.api.model

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.*

const val BOOKING_CREATED = "BOOKING_CREATED"

@DomainEvent(name = BOOKING_CREATED)
data class BookingCreatedEvent(
    val bookingId: UUID,
    val failedItems: Set<UUID>,
) : Event<BookingAggregate>(name = BOOKING_CREATED)
