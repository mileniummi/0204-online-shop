package com.itmo.microservices.demo.delivery.api.model

import java.util.*

class DeliveryLog (
    val outcome: DeliverySubmissionOutcome,
    val preparedTime: Long,
    val attempts: Int = 0,
    val submittedTime: Long? = null,
    val transactionId: UUID?,
    val submissionStartedTime: Long? = null,
)

enum class DeliverySubmissionOutcome {
    SUCCESS,
    FAILURE,
    EXPIRED
}