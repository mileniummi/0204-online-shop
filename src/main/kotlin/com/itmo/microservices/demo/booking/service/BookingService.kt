package com.itmo.microservices.demo.booking.service;

import com.itmo.microservices.demo.booking.api.model.BookingAggregate
import com.itmo.microservices.demo.booking.logic.BookingAggregateState
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.util.*

@Service
class BookingService (
        private val bookingEsService: EventSourcingService<UUID, BookingAggregate, BookingAggregateState>
) {


}
