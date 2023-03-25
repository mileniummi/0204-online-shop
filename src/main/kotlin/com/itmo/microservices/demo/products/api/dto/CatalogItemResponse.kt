package com.itmo.microservices.demo.products.api.dto

import java.math.BigDecimal
import java.util.*

data class CatalogItemResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val price: BigDecimal,
    val amount: Int, // number of items allowed for booking
)