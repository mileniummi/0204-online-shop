package com.itmo.microservices.demo.catalog.api.model

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "catalog")
class CatalogAggregate :Aggregate