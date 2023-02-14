package com.itmo.microservices.demo.users.api.model

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event

const val USER_CREATED = "USER_CREATED"

@DomainEvent(name = USER_CREATED)
data class UserCreatedEvent(
    val username: String?,
    val firstname: String?,
    val surname: String?,
    val email: String?,
    val password: String?
) : Event<UserAggregate>(name = USER_CREATED)