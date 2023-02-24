package com.itmo.microservices.demo.booking.projections

import com.itmo.microservices.demo.booking.api.model.BookingAggregate
import com.itmo.microservices.demo.booking.api.model.BookingCreatedEvent
import com.itmo.microservices.demo.booking.api.model.BookingLogCreatedEvent
import com.itmo.microservices.demo.booking.api.model.BookingStatus
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
class BookingExistingCache(
    private val bookingLogRepository: BookingLogRepository,
    private val bookingRepository: BookingRepository,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {
    private var logger: Logger = LoggerFactory.getLogger(BookingExistingCache::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(BookingAggregate::class, "transactions::booking-cache") {
            `when`(BookingCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    bookingRepository.save(BookingCache(id = event.id, bookingLogs = event.bookingLogs))
                }
                logger.info("Created booking with id ${event.id}")

            }
            `when`(BookingLogCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    bookingLogRepository.save(
                        BookingLogCache(
                            bookingLogId = event.id,
                            itemId = event.itemId,
                            status = event.status,
                            amount = event.amount,
                            timestamp = event.timestamp
                        )
                    )
                }
                logger.info("Created booking log with id ${event.id}")

            }
        }
    }

}

@Document("transactions-booking-cache")
data class BookingCache(
    @Id
    val id: UUID,
    val bookingLogs: Set<UUID>
)

@Document("transactions-booking-log-cache")
data class BookingLogCache(
    @Id
    val bookingLogId: UUID,
    val itemId: UUID,
    val status: BookingStatus,
    val amount: Int,
    val timestamp: Long
)

interface BookingRepository : MongoRepository<BookingCache, UUID> {
}

interface BookingLogRepository : MongoRepository<BookingLogCache, UUID> {
}