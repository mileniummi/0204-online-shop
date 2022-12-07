package com.itmo.microservices.demo.products.logic

import com.itmo.microservices.demo.products.api.model.ProductAggregate
import com.itmo.microservices.demo.products.api.model.ProductCreatedEvent
import ru.quipy.domain.AggregateState
import java.math.BigDecimal
import java.util.*

abstract class Product :
    AggregateState<UUID, ProductAggregate> {

    fun createNewProduct(
        id: UUID = UUID.randomUUID(), productName: String,
        price: BigDecimal,
        description: String
    ): ProductCreatedEvent {
        return ProductCreatedEvent(id, productName, price, description)
    }
}