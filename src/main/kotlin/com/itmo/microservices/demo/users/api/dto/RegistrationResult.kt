package com.itmo.microservices.demo.users.api.dto

import java.util.UUID
data class RegistrationResult(val id: UUID, val name: String)