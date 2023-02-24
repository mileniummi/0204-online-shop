package com.itmo.microservices.demo.booking.logic

import com.itmo.microservices.demo.booking.api.model.BookingAggregate
import com.itmo.microservices.demo.booking.api.model.BookingCreatedEvent
import com.itmo.microservices.demo.booking.api.model.BookingLogCreatedEvent
import com.itmo.microservices.demo.booking.api.model.BookingStatus
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

class BookingAggregateState : AggregateState<UUID, BookingAggregate>{
    private lateinit var bookingId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    private var bookingLogSet = mutableSetOf<UUID>()
    private var failedBookingLogSet = mutableSetOf<UUID>()

    override fun getId() = bookingId
    fun getFailedBookingLogs() = bookingLogSet
    fun createBookingItem(): BookingCreatedEvent {
        return BookingCreatedEvent(UUID.randomUUID(), setOf())
    }

    fun createBookingLog(itemId: UUID, amount: Int, status: BookingStatus): BookingLogCreatedEvent {
        return BookingLogCreatedEvent(
            bookingLogId = UUID.randomUUID(),
            itemId = itemId,
            amount = amount,
            timestamp = System.currentTimeMillis(),
            status = status
        )
    }

    @StateTransitionFunc
    fun createBookingItem(event: BookingCreatedEvent) {
        bookingId = event.bookingId
    }

    @StateTransitionFunc
    fun createBookingLog(event: BookingLogCreatedEvent) {
        bookingLogSet.add(event.id)
        if (event.status === BookingStatus.FAILED) {
            failedBookingLogSet.add(event.id)
        }
    }
}