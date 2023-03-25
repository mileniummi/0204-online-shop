package com.itmo.microservices.demo.payment.api.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.*

enum class FinancialOperationType {
    WITHDRAW, REFUND
}

class UserAccountFinancialLogRecordDto(
    val type: FinancialOperationType,
    val amount: BigDecimal,
    val orderId: UUID,
    val paymentTransactionId: UUID,
    val timestamp: Long
) {}