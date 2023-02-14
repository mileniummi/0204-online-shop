package com.itmo.microservices.demo.users.projections

import com.fasterxml.jackson.annotation.JsonIgnore
import com.itmo.microservices.demo.users.api.model.UserAggregate
import com.itmo.microservices.demo.users.api.model.UserCreatedEvent
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
class UserExistingCache(
    private val userRepository: UserCacheRepository,
    private val subscriptionsManager: AggregateSubscriptionsManager
) {
    private var logger: Logger = LoggerFactory.getLogger(UserCache::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "transactions::user-cache") {
            `when`(UserCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    userRepository.save(
                        UserCache(
                            event.id,
                            event.username,
                            event.firstname,
                            event.surname,
                            event.email,
                            event.password
                        )
                    )
                }
                logger.info("Created user with name ${event.username} and id ${event.id}")
            }
        }
    }

}


@Document("transactions-user-cache")
data class UserCache(
    @Id
    val id: UUID,
    val username: String?,
    val firstName: String?,
    val surname: String?,
    val email: String?,
    @JsonIgnore
    val password: String?
)

interface UserCacheRepository : MongoRepository<UserCache, UUID> {
    fun getUserCacheByUsername(username: String): Optional<UserCache>
}