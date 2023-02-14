package com.itmo.microservices.demo.orders.api.model

import java.util.*

class Order(
    val timeCreated: Long,
    val status: OrderStatus,
    val itemsMap: Map<UUID, Int>,
    val deliveryDuration: Int?
)

enum class OrderStatus { COLLECTING, DISCARD, BOOKED, PAID, SHIPPING, REFUND, COMPLETED }