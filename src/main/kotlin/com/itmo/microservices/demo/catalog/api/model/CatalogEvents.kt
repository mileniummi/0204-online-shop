package com.itmo.microservices.demo.catalog.api.model

import com.itmo.microservices.demo.products.api.model.Product
import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.util.UUID

const val CATALOG_ITEM_CREATED = "CATALOG_ITEM_CREATED"

@DomainEvent(name = CATALOG_ITEM_CREATED)
data class CatalogItemCreatedEvent(val catalogItemId: UUID, val product: Product, val amount: Int) :
    Event<CatalogAggregate>(name = CATALOG_ITEM_CREATED)