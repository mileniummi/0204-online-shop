package com.itmo.microservices.demo.orders.api.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.payment.api.dto.UserAccountFinancialLogRecordDto
import java.time.Duration
import java.util.*


class CreateOrderRequest(
)

data class OrderItem(
    val id: UUID,
    val title: String,
    val price: Int = 100
)


data class OrderResponse(
    val id: UUID,
    val timeCreated: Long,
//    @JsonDeserialize(using = OrderStatusDeserializer::class)
    val status: OrderStatus = OrderStatus.COLLECTING,
//    @JsonDeserialize(keyUsing = OrderKeyDeserializer::class)
    val itemsMap: Map<OrderItem, Int>, //
    val deliveryDuration: Duration? = null, //
    val paymentHistory: List<UserAccountFinancialLogRecordDto>
)