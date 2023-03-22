package com.itmo.microservices.demo.external.communicator

import com.itmo.microservices.demo.OnlineShopDescriptor
import com.itmo.microservices.demo.common.exceptions.ExternalServiceException
import java.util.UUID
import java.util.concurrent.ExecutorService

class SecretAwareExternalServiceApiCommunicator(descriptor: OnlineShopDescriptor, executor: ExecutorService) :
    ExternalServiceApiCommunicator(descriptor, executor) {
    private lateinit var clientSecret:UUID
    override suspend fun createProject(name: String): CreateProjectResponse {
        val response =  super.createProject(name) ?: throw ExternalServiceException("Can not create Project")
        clientSecret = response.accounts[0].clientSecret
        return  response
    }

    fun getClientSecret() = clientSecret
}