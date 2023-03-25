package com.itmo.microservices.demo.delivery.api.service

import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.delivery.api.model.DeliveryLog
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.time.Instant
import java.util.*

@Service
class DeliveryService(
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
) {
    fun getSlots(amount: Int): List<Int> {
        return (0 to amount).toList()
    }

    fun getDeliveryHistory(orderId: UUID): List<DeliveryLog> {
        val order = orderEsService.getState(orderId) ?: throw NotFoundException("No order $orderId")
        return order.deliveryHistory
    }
}