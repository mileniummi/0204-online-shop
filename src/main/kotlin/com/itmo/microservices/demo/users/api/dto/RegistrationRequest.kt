package com.itmo.microservices.demo.users.api.dto

data class RegistrationRequest(
        val name: String,
        val password: String
)