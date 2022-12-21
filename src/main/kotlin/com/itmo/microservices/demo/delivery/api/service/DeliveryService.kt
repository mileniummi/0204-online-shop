package com.itmo.microservices.demo.delivery.api.service

import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DeliveryService {
    fun getSlots(amount: Int): List<Instant> {
        return generateSequence(Instant.now()) { it.plusSeconds(20) }.take(amount).toList()
    }
}