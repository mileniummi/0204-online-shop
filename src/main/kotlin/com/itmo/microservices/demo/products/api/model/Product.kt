package com.itmo.microservices.demo.products.api.model

import java.math.BigDecimal
import java.util.*

class Product(
    val productId: UUID,
    val productName: String,
    val price: BigDecimal,
    val description: String
)