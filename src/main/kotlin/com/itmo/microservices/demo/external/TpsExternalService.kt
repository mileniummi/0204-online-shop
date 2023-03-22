package com.itmo.microservices.demo.external
import org.springframework.stereotype.Service


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