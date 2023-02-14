package com.itmo.microservices.demo.payment.logic

import com.itmo.microservices.demo.payment.api.model.PaymentRecordLogAggregate
import ru.quipy.domain.AggregateState
import java.util.UUID

class PaymentRecordLogAggregateState: AggregateState<UUID, PaymentRecordLogAggregate> {
    private lateinit var paymentRecordLogId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    override fun getId() = paymentRecordLogId
}