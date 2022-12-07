package com.itmo.microservices.demo.products.api.model

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "products")
class ProductAggregate: Aggregate