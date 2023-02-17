package com.itmo.microservices.demo.booking.api.model

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "booking")
class BookingAggregate : Aggregate