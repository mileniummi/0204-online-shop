package com.itmo.microservices.demo.users.projections

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

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
    private var logger: Logger = LoggerFactory.getLogger(AppUserCache::class.java)

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(UserAggregate::class, "transactions::user-cache") {
            `when`(UserCreatedEvent::class) { event ->
                withContext(Dispatchers.IO) {
                    userRepository.save(
                        AppUserCache(
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
data class AppUserCache(
    @Id
    val id: UUID? = null,
    val username: String,
    val name: String,
    val surname: String,
    val email: String,
    @JsonIgnore
    var password: String
) {
    fun userDetails(): UserDetails = User(username, password, emptyList())
}

interface UserCacheRepository : MongoRepository<AppUserCache, UUID> {
    fun getByUsername(username: String): Optional<AppUserCache>
}