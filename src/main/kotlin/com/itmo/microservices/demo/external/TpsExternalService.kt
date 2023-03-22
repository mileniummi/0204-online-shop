package com.itmo.microservices.demo.external

import com.itmo.microservices.demo.OnlineShopDescriptor
import com.itmo.microservices.demo.external.communicator.SecretAwareExternalServiceApiCommunicator
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ForkJoinPool
import javax.annotation.PostConstruct

@Service
class TpsExternalService(descriptor: OnlineShopDescriptor) {
    private val executorService = ForkJoinPool()
    private val communicator = SecretAwareExternalServiceApiCommunicator(descriptor, executorService)
    // todo (ask how to implement)
//    @PostConstruct
    suspend fun createProject(name: String) {
        communicator.createProject(name)
    }

    private fun getProjectClientSecret() = communicator.getClientSecret()

    suspend fun getDeliverySlot(address: UUID): Any {
        val secret = getProjectClientSecret()

        return communicator.execute("getDeliverySlot", "/transactions/delivery") {
            jsonPost(
                "clientSecret" to secret.toString(),
                "address" to address.toString()
            )
        }

    }
}