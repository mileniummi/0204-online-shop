package com.itmo.microservices.demo.payment.api.model

import ru.quipy.core.annotations.AggregateType
import ru.quipy.domain.Aggregate

@AggregateType(aggregateEventsTableName = "paymentRecordLog")
class PaymentRecordLogAggregate : Aggregate