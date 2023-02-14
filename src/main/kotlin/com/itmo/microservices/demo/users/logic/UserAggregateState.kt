package com.itmo.microservices.demo.users.logic

import com.itmo.microservices.demo.users.api.model.UserAggregate
import com.itmo.microservices.demo.users.api.model.UserCreatedEvent
import com.itmo.microservices.demo.users.impl.entity.AppUser
import ru.quipy.core.annotations.StateTransitionFunc
import ru.quipy.domain.AggregateState
import java.util.UUID

class UserAggregateState : AggregateState<UUID, UserAggregate> {
    private lateinit var userId: UUID
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    override fun getId() = userId

    fun registerUser(user: AppUser): UserCreatedEvent {
        return UserCreatedEvent(user.username, user.name, user.surname, user.email, user.password)
    }

    @StateTransitionFunc
    fun registerUser(event: UserCreatedEvent) {
        userId = event.id
    }
}