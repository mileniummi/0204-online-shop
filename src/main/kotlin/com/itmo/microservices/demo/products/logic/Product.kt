package com.itmo.microservices.demo.products.logic

import com.itmo.microservices.demo.products.api.ProductAggregate
import ru.quipy.domain.AggregateState
import java.math.BigDecimal
import java.util.*

class ProductAgg: AggregateState<UUID, ProductAggregate> {
    override val aggregateId: UUID
        get() = TODO("Not yet implemented")
    override var createdAt: Long = System.currentTimeMillis()
    override var updatedAt: Long = System.currentTimeMillis()
}

data class Product(
    var name: String,
    var description: String,
    var price: BigDecimal){
}