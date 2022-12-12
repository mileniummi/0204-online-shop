package com.itmo.microservices.demo.orders.api.dto

import java.util.*

class CreateOrderRequest(
    val itemsMap: Map<UUID, Int>,
)