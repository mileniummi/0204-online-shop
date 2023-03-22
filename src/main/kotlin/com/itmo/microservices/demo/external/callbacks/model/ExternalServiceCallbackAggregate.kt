package com.itmo.microservices.demo.external.callbacks.model

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "external-service-callback")
class ExternalServiceCallbackAggregate: Aggregate