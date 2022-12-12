package com.itmo.microservices.demo.catalog.logic

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.api.model.CatalogItemCreatedEvent
import com.itmo.microservices.demo.products.api.model.Product
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.UUID

class CatalogAggregateState: AggregateState<UUID, CatalogAggregate> {
    private lateinit var catalogId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
    override fun getId(): UUID = catalogId

    fun createCatalogItem(product: Product,  amount: Int):CatalogItemCreatedEvent {
        return CatalogItemCreatedEvent(UUID.randomUUID(), product, amount)
    }

    @StateTransitionFunc
    fun createCatalogItem(event: CatalogItemCreatedEvent ){
        catalogId = event.catalogItemId
        updatedAt = createdAt
    }
}