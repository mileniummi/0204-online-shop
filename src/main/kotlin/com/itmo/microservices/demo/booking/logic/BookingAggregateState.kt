package com.itmo.microservices.demo.booking.logic

import com.itmo.microservices.demo.booking.api.model.BookingAggregate
import com.itmo.microservices.demo.booking.api.model.BookingCreatedEvent
import com.itmo.microservices.demo.booking.api.model.BookingStatus
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

class BookingAggregateState : AggregateState<UUID, BookingAggregate> {
    private lateinit var bookingId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
// status?
    private var status: BookingStatus = BookingStatus.SUCCESS

    override fun getId() = bookingId

    fun createCatalogItem(failedItems: Set<UUID>): BookingCreatedEvent {
        return BookingCreatedEvent(UUID.randomUUID(), setOf())
    }

    @StateTransitionFunc
    fun createCatalogItem(event: BookingCreatedEvent) {
        bookingId = event.bookingId
    }
}