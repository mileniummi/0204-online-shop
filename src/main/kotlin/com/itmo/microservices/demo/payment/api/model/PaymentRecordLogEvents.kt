package com.itmo.microservices.demo.payment.api.model

import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

const val PAYMENT_SUCCESS = "PAYMENT_SUCCESS"
const val PAYMENT_FAILED = "PAYMENT_FAILED"


@DomainEvent(name = PAYMENT_SUCCESS)
data class PaymentSuccessEvent(
    val orderId: UUID,
    val timestamp: Instant,
    val status: PaymentStatus,
    val amount: BigDecimal,
    val transactionId: UUID
): Event<OrderAggregate>(name = PAYMENT_SUCCESS)