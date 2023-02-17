package com.itmo.microservices.demo.users.impl.service

import com.itmo.microservices.demo.common.exceptions.EntityViolationException
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.users.api.service.UserService
import com.itmo.microservices.demo.users.api.dto.RegistrationRequest
import com.itmo.microservices.demo.users.api.model.UserAggregate
import com.itmo.microservices.demo.users.api.model.UserCreatedEvent
import com.itmo.microservices.demo.users.logic.UserAggregateState
import com.itmo.microservices.demo.users.projections.AppUserCache
import com.itmo.microservices.demo.users.projections.UserCacheRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.util.UUID

@Suppress("UnstableApiUsage")
@Service
class DefaultUserService(
    private val passwordEncoder: PasswordEncoder,
    private val userCacheRepository: UserCacheRepository,
    private val userEsService: EventSourcingService<UUID, UserAggregate, UserAggregateState>
) : UserService {

    override fun findUser(username: String): AppUserCache =
        userCacheRepository.getByUsername(username).orElseThrow { NotFoundException("User does not exist") }

    override fun registerUser(request: RegistrationRequest): ResponseEntity<UserCreatedEvent> {
        val dbUser = userCacheRepository.getByUsername(request.username)
        if (dbUser.isPresent) {
            throw EntityViolationException("This username is already taken")
        }
        return ResponseEntity(
            userEsService.create { it.registerUser(request.toEntity()) }, HttpStatus.CREATED
        )
    }

    override fun getAccountData(requester: UserDetails) = findUser(requester.username)

    override fun deleteUser(user: UserDetails) {
        throw NotImplementedError()
        // todo rewrite this in tiny-es
//        runCatching {
//            userRepository.deleteById(user.username)
//        }.onSuccess {
//            eventBus.post(UserDeletedEvent(user.username))
//            eventLogger.info(UserServiceNotableEvents.I_USER_DELETED, user.username)
//        }.onFailure {
//            throw NotFoundException("User ${user.username} not found", it)
//        }
    }

    fun RegistrationRequest.toEntity(): AppUserCache =
        AppUserCache(
            username = this.username,
            name = this.name,
            surname = this.surname,
            email = this.email,
            password = passwordEncoder.encode(this.password)
        )
}
