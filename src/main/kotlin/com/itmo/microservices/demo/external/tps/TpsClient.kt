package com.itmo.microservices.demo.external.tps
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.UUID

class TpsClient {
    private val clientSecret = UUID.fromString(System.getenv("EXTERNAL_SYSTEM_CLIENT_SECRET"))
    private val baseUrl = System.getenv("EXTERNAL_SYSTEM_URL")
    private val client = HttpClient(CIO) {
        install(JsonFeature)
    }

    suspend fun createPayment(amount: Double): ResponseDto {
        return createRequest(PaymentRequestDto(clientSecret, amount))
    }

    suspend fun createNotification(email: String): ResponseDto {
        return createRequest(NotificationRequestDto(clientSecret, email))
    }

    suspend fun createDelivery(address: String): ResponseDto {
        return createRequest(DeliveryRequestDto(clientSecret, address))
    }

    private suspend fun createRequest(reqBody: Any): ResponseDto {
        return client.post("${baseUrl}/transactions/payment") {
            contentType(ContentType.Application.Json)
            body = reqBody
        }
    }
}

@JsonSerialize
private data class PaymentRequestDto(val clientSecret: UUID, val sum: Double)

@JsonSerialize
private data class NotificationRequestDto(val clientSecret: UUID, val email: String)

@JsonSerialize
private data class DeliveryRequestDto(val clientSecret: UUID, val address: String)


@JsonSerialize
data class ResponseDto(
    val id: UUID,
    val status: String,
    val submitTime: Long,
    val completedTime: Long?,
    val cost: Double?,
    val delta: Long?
)
