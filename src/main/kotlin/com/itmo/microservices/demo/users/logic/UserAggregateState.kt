package com.itmo.microservices.demo.users.logic

import com.itmo.microservices.demo.users.api.model.UserAggregate
import com.itmo.microservices.demo.users.api.model.UserCreatedEvent
import com.itmo.microservices.demo.users.api.model.UserDeletedEvent
import com.itmo.microservices.demo.users.impl.entity.AppUser
import com.itmo.microservices.demo.users.projections.AppUserCache
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.UUID

class UserAggregateState : AggregateState<UUID, UserAggregate> {
    private lateinit var userId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    override fun getId() = userId

    fun registerUser(user: AppUserCache): UserCreatedEvent  =
        UserCreatedEvent(user.username, user.name, user.surname, user.email, user.password)

    fun deleteUser(username: String): UserDeletedEvent =
        UserDeletedEvent(username)

    @StateTransitionFunc
    fun registerUser(event: UserCreatedEvent) {
        userId = event.id
    }

    @StateTransitionFunc
    fun deleteUser(event: UserDeletedEvent) {
        userId = event.id
    }
}