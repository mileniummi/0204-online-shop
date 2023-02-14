package com.itmo.microservices.demo.users.api.service

import com.itmo.microservices.demo.users.api.model.AppUserModel
import com.itmo.microservices.demo.users.api.dto.RegistrationRequest
import com.itmo.microservices.demo.users.api.model.UserCreatedEvent
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails

interface UserService {
    fun findUser(username: String): AppUserModel?
    fun registerUser(request: RegistrationRequest): ResponseEntity<UserCreatedEvent>
    fun getAccountData(requester: UserDetails): AppUserModel
    fun deleteUser(user: UserDetails)
}