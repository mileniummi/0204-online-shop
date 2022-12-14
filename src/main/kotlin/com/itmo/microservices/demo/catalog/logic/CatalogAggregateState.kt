package com.itmo.microservices.demo.catalog.logic

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.api.model.CatalogItemAmountUpdatedEvent
import com.itmo.microservices.demo.catalog.api.model.CatalogItemCreatedEvent
import com.itmo.microservices.demo.products.api.model.Product
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.UUID

class CatalogAggregateState : AggregateState<UUID, CatalogAggregate> {
    private lateinit var catalogId: UUID
    var amount: Int = 0
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()
    override fun getId(): UUID = catalogId

    fun createCatalogItem(product: Product, amount: Int): CatalogItemCreatedEvent {
        return CatalogItemCreatedEvent(UUID.randomUUID(), product, amount)
    }

    fun updateCatalogItemAmount(id: UUID, amount: Int, isIncreased: Boolean = true) : CatalogItemAmountUpdatedEvent {
        if (isIncreased){
            return CatalogItemAmountUpdatedEvent(id, this.amount + amount )
        }
        val newAmount = this.amount - amount
        if (newAmount < 0){
            throw IllegalStateException("Attempt to take more item amount than exists.")
        }
        return CatalogItemAmountUpdatedEvent(catalogId, newAmount )
    }

    @StateTransitionFunc
    fun createCatalogItem(event: CatalogItemCreatedEvent) {
        catalogId = event.catalogItemId
        amount = event.amount
        updatedAt = createdAt
    }
    @StateTransitionFunc
    fun updateCatalogItemAmount(event: CatalogItemAmountUpdatedEvent) {
        catalogId = event.catalogItemId
        amount = event.amount
        updatedAt = createdAt
    }
}