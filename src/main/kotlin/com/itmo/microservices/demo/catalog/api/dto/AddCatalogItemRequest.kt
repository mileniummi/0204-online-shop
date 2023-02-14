package com.itmo.microservices.demo.catalog.api.dto

import java.math.BigDecimal

class AddCatalogItemRequest(
    val name: String,
    val description: String,
    val price: BigDecimal, val amount: Int,
)