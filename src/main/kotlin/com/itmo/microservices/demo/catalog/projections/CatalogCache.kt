package com.itmo.microservices.demo.catalog.projections

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.api.model.CatalogItemAmountUpdatedEvent
import com.itmo.microservices.demo.catalog.api.model.CatalogItemCreatedEvent
import com.itmo.microservices.demo.products.api.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Component
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct


@Component
class CatalogExistingCache(
    private val catalogItemCacheRepository: CatalogItemCacheRepository,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {
    private var logger: Logger = LoggerFactory.getLogger(CatalogExistingCache::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(CatalogAggregate::class, "transactions::catalog-item-cache") {
            `when`(CatalogItemCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    catalogItemCacheRepository.save(CatalogItem(event.catalogItemId, event.product, event.amount))
                }
                logger.info(
                    "Update catalog with item ${event.catalogItemId} containing product " +
                            "${event.product.productName}-${event.product.productId} with amount ${event.amount}"
                )
            }
            `when`(CatalogItemAmountUpdatedEvent::class) { event ->
                val catalogItem = withContext(Dispatchers.IO) {
                    catalogItemCacheRepository.findById(event.catalogItemId)
                }
                if (catalogItem.isPresent) {
                    catalogItem.get().amount = event.amount
                    withContext(Dispatchers.IO) {
                        catalogItemCacheRepository.save(catalogItem.get())
                    }
                }
                logger.info(
                    "Update catalog with item ${event.catalogItemId} with amount ${event.amount}"
                )
            }
        }
    }
}

@Document("transactions-catalog-item-cache")
data class CatalogItem(
    @Id
    val id: UUID,
    val product: Product,
    var amount: Int,
)
interface CatalogItemCacheRepository : MongoRepository<CatalogItem, UUID> {
    @Query("{ amount: {\$gt: 0}}")
    fun findAllWithExistingAmount(pageable: Pageable): Page<CatalogItem>
}