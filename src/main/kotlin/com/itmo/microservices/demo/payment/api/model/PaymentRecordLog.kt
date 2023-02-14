package com.itmo.microservices.demo.payment.api.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

enum class PaymentStatus { FAILED, SUCCESS }

class PaymentRecordLog(
    val timestamp: Instant,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val transactionId: UUID
)