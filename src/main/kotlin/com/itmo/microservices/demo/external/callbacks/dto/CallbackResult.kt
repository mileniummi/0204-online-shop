package com.itmo.microservices.demo.external.callbacks.dto

import java.util.UUID

data class CallbackResult(
    val id: UUID,
    val status: CallbackStatus,
    val submitTime: Long,
    val completedTime: Long?,
    val cost: Double?,
    val delta: Long?
)

enum class CallbackStatus {SUCCESS, FAILURE, PENDING}
