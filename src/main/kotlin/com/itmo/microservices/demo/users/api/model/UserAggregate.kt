package com.itmo.microservices.demo.users.api.model

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate


@AggregateType(aggregateEventsTableName = "user")
class UserAggregate : Aggregate