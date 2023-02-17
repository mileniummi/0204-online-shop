package com.itmo.microservices.demo.booking.logic

import com.itmo.microservices.demo.booking.api.model.BookingAggregate
import com.itmo.microservices.demo.booking.api.model.BookingStatus
import ru.quipy.domain.AggregateState
import java.util.*

class BookingAggregateState : AggregateState<UUID, BookingAggregate> {
    private lateinit var bookingId: UUID
// status?
    private var status: BookingStatus = BookingStatus.SUCCESS

    override fun getId() = bookingId

}