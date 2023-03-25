package com.itmo.microservices.demo.booking.logic

import com.itmo.microservices.demo.booking.api.model.*
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.*

class BookingAggregateState : AggregateState<UUID, BookingAggregate>{
    private lateinit var bookingId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    private var bookingLogSet: MutableList<BookingLogRecord> = mutableListOf()
    private var failedBookingLogSet: MutableList<BookingLogRecord> = mutableListOf()

    override fun getId() = bookingId
    fun getBookingLogs() = bookingLogSet
    fun getFailedBookingLogs() = failedBookingLogSet
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
        val log = BookingLogRecord(
            bookingId = bookingId,
            bookingLogId = event.bookingLogId,
            itemId = event.itemId,
            amount = event.amount,
            timestamp = System.currentTimeMillis(),
            status = event.status
        )
        bookingLogSet.add(log)
        if (event.status === BookingStatus.FAILED) {
            failedBookingLogSet.add(log)
        }
    }
}