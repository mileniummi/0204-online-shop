package com.itmo.microservices.demo.products.api.model

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.math.BigDecimal
import java.util.UUID

const val PRODUCT_CREATED = "PRODUCT_CREATED_EVENT"

@DomainEvent(name = PRODUCT_CREATED)
data class ProductCreatedEvent(
    val productId: UUID,
    val productName: String,
    val price: BigDecimal,
    val description: String
) : Event<ProductAggregate>(
    name = PRODUCT_CREATED
)