package com.itmo.microservices.demo.external.callbacks.model

import com.itmo.microservices.demo.external.callbacks.dto.CallbackStatus
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.UUID

const val EXTERNAL_SERVICE_CALLBACK_CREATED = "EXTERNAL_SERVICE_CALLBACK_CREATED"

@DomainEvent(name = EXTERNAL_SERVICE_CALLBACK_CREATED)
data class ExternalServiceCallbackCreatedEvent(
    val transactionId: UUID,
    val status: CallbackStatus
): Event<ExternalServiceCallbackAggregate>(name = EXTERNAL_SERVICE_CALLBACK_CREATED)