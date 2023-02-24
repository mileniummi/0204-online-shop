package com.itmo.microservices.demo.payment.api.dto

import java.time.Instant
import java.util.*

class PaymentSubmissionDto (
    val timestamp: Instant,
    val transactionId: UUID
) {}