package com.itmo.microservices.demo.orders.api.model

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "order")
class OrderAggregate :Aggregate