package com.itmo.microservices.demo.users.api.service

import com.itmo.microservices.demo.users.api.dto.RegistrationRequest
import com.itmo.microservices.demo.users.api.dto.RegistrationResult
import com.itmo.microservices.demo.users.projections.AppUserCache
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails

interface UserService {
    fun findUser(username: String): AppUserCache
    fun registerUser(request: RegistrationRequest): ResponseEntity<RegistrationResult>
    fun getAccountData(requester: UserDetails): AppUserCache
    fun deleteUser(user: UserDetails)
}