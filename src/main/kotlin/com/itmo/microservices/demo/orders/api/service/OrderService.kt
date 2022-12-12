package com.itmo.microservices.demo.orders.api.service

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.common.exception.EntityViolationException
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderCreatedEvent
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.util.*

@Service
class OrderService(
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>
) {

    fun createOrder(order: CreateOrderRequest): ResponseEntity<OrderCreatedEvent> {
        for (orderCatalogItem in order.itemsMap) {
            val catalogItem = catalogEsService.getState(orderCatalogItem.key)
                ?: throw NotFoundException("Order item does not exist.")
            if (orderCatalogItem.value > catalogItem.amount) {
                throw EntityViolationException("Order item amount is greater than existing item amount in warehouse.")
            }
        }
        return ResponseEntity(
            orderEsService.create { orderState: OrderAggregateState -> orderState.createOrder(order) },
            HttpStatus.CREATED
        )
    }
}