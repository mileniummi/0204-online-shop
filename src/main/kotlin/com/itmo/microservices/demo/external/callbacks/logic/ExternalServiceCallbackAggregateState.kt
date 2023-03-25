package com.itmo.microservices.demo.external.callbacks.logic

import com.itmo.microservices.demo.external.callbacks.dto.CallbackResult
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackAggregate
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackCreatedEvent
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.UUID

class ExternalServiceCallbackAggregateState : AggregateState<UUID, ExternalServiceCallbackAggregate> {
    private lateinit var callbackId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    override fun getId() = callbackId

    fun createCallback(result: CallbackResult): ExternalServiceCallbackCreatedEvent =
        ExternalServiceCallbackCreatedEvent(result.id, result.status)

    @StateTransitionFunc
    fun createCallback(event: ExternalServiceCallbackCreatedEvent) {
        callbackId = event.transactionId
    }
}