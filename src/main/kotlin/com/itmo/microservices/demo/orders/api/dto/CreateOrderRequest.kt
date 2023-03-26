package com.itmo.microservices.demo.orders.api.dto

import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.payment.api.model.PaymentRecordLog
import java.math.BigDecimal
import java.util.*


class CreateOrderRequest(
    val itemsMap: Map<UUID, Int>?
)

data class OrderItem(
    val id: UUID,
    val title: String,
    val price: BigDecimal
)


data class OrderResponse(
    val id: UUID,
    val timeCreated: Long,
//    @JsonDeserialize(using = OrderStatusDeserializer::class)
    val status: OrderStatus = OrderStatus.COLLECTING,
//    @JsonDeserialize(keyUsing = OrderKeyDeserializer::class)
    val itemsMap: Map<UUID, Int>, //
    val deliveryDuration: Int? = null, //
    val paymentHistory: List<PaymentRecordLog>
)