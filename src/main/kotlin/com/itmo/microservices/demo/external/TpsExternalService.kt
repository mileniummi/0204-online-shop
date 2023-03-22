package com.itmo.microservices.demo.external

import com.itmo.microservices.demo.OnlineShopDescriptor
//import com.itmo.microservices.demo.external.communicator.SecretAwareExternalServiceApiCommunicator
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ForkJoinPool
import javax.annotation.PostConstruct


// TODO: add retries on error, better interface, not ResponseDto
@Service
class TpsExternalService() {
    private val client = TpsClient()

    suspend fun createPayment(amount: Double): ResponseDto {
        return client.createPayment(amount)
    }

    suspend fun createNotification(email: String): ResponseDto {
        return client.createNotification(email)
    }

    suspend fun createDelivery(address: String): ResponseDto {
        return client.createDelivery(address)
    }

}