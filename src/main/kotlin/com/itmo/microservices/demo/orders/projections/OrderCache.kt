package com.itmo.microservices.demo.orders.projections

import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderCreatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderDeliverySlotUpdatedEvent
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.payment.api.model.PaymentRecordLog
import com.itmo.microservices.demo.payment.api.model.PaymentSuccessEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Component
import ru.quipy.streams.AggregateSubscriptionsManager
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

@Component
class OrderExistingCache(
    private val orderRepository: OrderRepository,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {
    private var logger: Logger = LoggerFactory.getLogger(OrderExistingCache::class.java)

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
                            event.itemsMap,
                            null,
                            mutableListOf()
                        )
                    )
                }
                logger.info("Update order with item ${event.orderId}")
            }
            `when`(OrderDeliverySlotUpdatedEvent::class) { event ->
                val order = withContext(Dispatchers.IO) {
                    orderRepository.findById(event.orderId)
                }
                if (order.isPresent) {
                    withContext(Dispatchers.IO) {
                        orderRepository.save(
                            OrderCache(
                                event.orderId,
                                order.get().timeCreated,
                                event.status,
                                order.get().itemsMap,
                                event.deliverySlot,
                                order.get().paymentHistory,
                            )
                        )
                    }
                }
                logger.info("Update order ${event.orderId} with delivery slot ${event.deliverySlot}")
            }
            `when`(PaymentSuccessEvent::class) { event ->
                val order = withContext(Dispatchers.IO) {
                    orderRepository.findById(event.orderId)
                }
                if (order.isPresent) {
                    withContext(Dispatchers.IO) {
                        orderRepository.save(
                            OrderCache(
                                event.orderId,
                                order.get().timeCreated,
                                OrderStatus.PAID,
                                order.get().itemsMap,
                                order.get().deliverySlot,
                                order.get().paymentHistory,
                            )
                        )
                    }
                }
                logger.info("Order ${event.orderId} was successfully paid")
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
    val deliverySlot: Instant? = null,
    var paymentHistory: MutableList<PaymentRecordLog>
)

interface OrderRepository : MongoRepository<OrderCache, UUID>{
    @Query("{ deliverySlot: {\$gte: ?0, \$lte: ?1}}")
    fun findByDeliverySlot(deliverySlotStart: Instant, deliverySlotEnd: Instant): Optional<OrderCache>
}