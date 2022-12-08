package com.itmo.microservices.demo.products.logic

import com.itmo.microservices.demo.products.api.model.ProductAggregate
import com.itmo.microservices.demo.products.api.model.ProductCreatedEvent
import ru.quipy.domain.AggregateState
import java.math.BigDecimal
import java.util.*

class Product(val aggregateId: UUID) : AggregateState<UUID, ProductAggregate> {
      var createdAt: Long = System.currentTimeMillis()
      var updatedAt: Long = System.currentTimeMillis()

    fun createNewProduct(
        id: UUID = UUID.randomUUID(), productName: String,
        price: BigDecimal,
        description: String
    ): ProductCreatedEvent {
        return ProductCreatedEvent(id, productName, price, description)
    }

    override fun getId(): UUID = aggregateId
}