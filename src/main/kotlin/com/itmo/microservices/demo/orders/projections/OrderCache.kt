package com.itmo.microservices.demo.orders.projections

import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.delivery.api.model.DeliveryLog
import com.itmo.microservices.demo.delivery.api.model.DeliverySubmissionOutcome
import com.itmo.microservices.demo.orders.api.model.*
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
                            id = event.orderId,
                            timeCreated = event.timeCreated,
                            status = event.status,
                            itemsMap = event.itemsMap,
                            deliverySlot = null,
                            paymentHistory = mutableListOf(),
                            deliveryHistory = mutableListOf(),
                        )
                    )
                }
                logger.info("Update order with item ${event.orderId}")
            }
            `when`(OrderUpdatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    val o = orderRepository.findById(event.orderId)
                        .orElseThrow{ NotFoundException("Order not found ${event.orderId}") }
                    val itemsMap = o.itemsMap.toMutableMap()
                    for(i in event.itemsMap)
                        itemsMap[i.key] = i.value
                    orderRepository.save(
                        OrderCache(
                            id = event.orderId,
                            timeCreated = o.timeCreated,
                            status = OrderStatus.COLLECTING,
                            itemsMap = itemsMap,
                            deliverySlot = o.deliverySlot,
                            paymentHistory = o.paymentHistory,
                            deliveryHistory = o.deliveryHistory,
                        )
                    )
                }
                logger.info("Update order with item ${event.orderId}")
            }
            `when`(OrderDeliverySlotUpdatedEvent::class) { event ->
                val order = withContext(Dispatchers.IO) {
                    orderRepository.findById(event.orderId)
                }
                val deliveryLog = DeliveryLog(
                    outcome = DeliverySubmissionOutcome.SUCCESS,
                    preparedTime = System.currentTimeMillis(),
                    attempts = 1,
                    submittedTime = System.currentTimeMillis(),
                    transactionId = event.transactionId,
                    submissionStartedTime = System.currentTimeMillis(),
                )
                order.get().deliveryHistory.add(deliveryLog)
                if (order.isPresent) {
                    withContext(Dispatchers.IO) {
                        orderRepository.save(
                            OrderCache(
                                id = event.orderId,
                                timeCreated = order.get().timeCreated,
                                status = event.status,
                                itemsMap = order.get().itemsMap,
                                deliverySlot = event.deliverySlot,
                                paymentHistory = order.get().paymentHistory,
                                deliveryHistory = order.get().deliveryHistory
                            )
                        )
                    }
                }
                logger.info("Update order ${event.orderId} with delivery slot ${event.deliverySlot}")
            }
            `when`(OrderUpdateAfterBooking::class) { event ->
                val order = withContext(Dispatchers.IO) {
                    orderRepository.findById(event.orderId)
                        .orElseThrow {NotFoundException("Order not found ${event.orderId}")}
                }
                withContext(Dispatchers.IO) {
                    orderRepository.save(
                        OrderCache(
                            id = order.id,
                            timeCreated = order.timeCreated,
                            status = event.status,
                            itemsMap = order.itemsMap,
                            deliverySlot = order.deliverySlot,
                            paymentHistory = order.paymentHistory,
                            deliveryHistory = order.deliveryHistory,
                        )
                    )
                }
                logger.info("Update order after booking ${event.orderId} ${event.bookingId} ${event.status}")
            }
            `when`(PaymentSuccessEvent::class) { event ->
                val order = withContext(Dispatchers.IO) {
                    orderRepository.findById(event.orderId)
                }
                order.get().paymentHistory.add(
                    PaymentRecordLog(event.timestamp, event.amount, event.status, event.transactionId)
                )
                if (order.isPresent) {
                    withContext(Dispatchers.IO) {
                        orderRepository.save(
                            OrderCache(
                                id = event.orderId,
                                timeCreated = order.get().timeCreated,
                                status = OrderStatus.PAID,
                                itemsMap = order.get().itemsMap,
                                deliverySlot = order.get().deliverySlot,
                                paymentHistory = order.get().paymentHistory,
                                deliveryHistory = order.get().deliveryHistory,
                            )
                        )
                    }
                }
                logger.info("Order ${event.orderId} was successfully paid")
            }
            `when`(OrderCompleted::class) { event ->
                val order = withContext(Dispatchers.IO) {
                    orderRepository.findById(event.orderId)
                }
                if (order.isPresent) {
                    withContext(Dispatchers.IO) {
                        orderRepository.save(
                            OrderCache(
                                id = event.orderId,
                                timeCreated = order.get().timeCreated,
                                status = OrderStatus.COMPLETED,
                                itemsMap = order.get().itemsMap,
                                deliverySlot = order.get().deliverySlot,
                                paymentHistory = order.get().paymentHistory,
                                deliveryHistory = order.get().deliveryHistory,
                            )
                        )
                    }
                }
                logger.info("Order ${event.orderId} was successfully completed")
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
    val deliverySlot: Int? = null,
    var paymentHistory: MutableList<PaymentRecordLog>,
    var deliveryHistory: MutableList<DeliveryLog>,
)

interface OrderRepository : MongoRepository<OrderCache, UUID>{
    @Query("{ deliverySlot: {\$gte: ?0, \$lte: ?1}}")
    fun findByDeliverySlot(deliverySlotStart: Int, deliverySlotEnd: Int): Optional<OrderCache>
}