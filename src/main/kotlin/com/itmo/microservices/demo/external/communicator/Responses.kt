package com.itmo.microservices.demo.external.communicator

import java.util.UUID

data class ProjectAccountsResponse(val name: String, val clientSecret: UUID)
data class CreateProjectResponse(val name: String, val accounts: List<ProjectAccountsResponse>)