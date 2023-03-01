package com.itmo.microservices.demo.users.api.model

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event

const val USER_CREATED = "USER_CREATED"
const val USER_DELETED = "USER_DELETED"

@DomainEvent(name = USER_CREATED)
data class UserCreatedEvent(
    val username: String,
    val password: String
) : Event<UserAggregate>(name = USER_CREATED)

@DomainEvent(name = USER_DELETED)
data class UserDeletedEvent(
    val username: String,
) : Event<UserAggregate>(name = USER_DELETED)