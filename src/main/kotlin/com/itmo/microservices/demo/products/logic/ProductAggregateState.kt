package com.itmo.microservices.demo.products.logic

import com.itmo.microservices.demo.products.api.model.ProductAggregate
import com.itmo.microservices.demo.products.api.model.ProductCreatedEvent
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.math.BigDecimal
import java.util.*

class ProductAggregateState : AggregateState<UUID, ProductAggregate> {
    private lateinit var productId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
    override fun getId(): UUID = productId

    fun createNewProduct(
        productName: String,
        price: BigDecimal,
        description: String
    ): ProductCreatedEvent {
        return ProductCreatedEvent(UUID.randomUUID(), productName, price, description)
    }

    @StateTransitionFunc
    fun createNewProduct(event: ProductCreatedEvent) {
        productId = event.productId
        updatedAt = createdAt
    }
}