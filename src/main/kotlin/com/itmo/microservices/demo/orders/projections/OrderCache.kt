package com.itmo.microservices.demo.orders.projections

import com.itmo.microservices.demo.catalog.projections.CatalogExistingCache
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderCreatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import ru.quipy.streams.AggregateSubscriptionsManager
import java.util.*
import javax.annotation.PostConstruct

@Component
class OrderExistingCache(
    private val orderRepository: OrderRepository,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {
    private var logger: Logger = LoggerFactory.getLogger(CatalogExistingCache::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(OrderAggregate::class, "transactions::order-cache") {
            `when`(OrderCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    orderRepository.save(
                        OrderCache(
                            event.orderId,
                            event.timeCreated,
                            event.status,
                            event.itemsMap)
                    )
                }
                logger.info("Update order with item ${event.orderId}")
            }
        }
    }

}

@Document("transactions-order-cache")
data class OrderCache(
    @Id
    val id: UUID,
    val timeCreated: Long,
    val status: OrderStatus,
    val itemsMap: Map<UUID, Int>,
    val deliveryDuration: Int? = 0
)

interface OrderRepository : MongoRepository<OrderCache, UUID>