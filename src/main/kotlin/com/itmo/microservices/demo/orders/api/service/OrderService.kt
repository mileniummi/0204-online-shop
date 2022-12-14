package com.itmo.microservices.demo.orders.api.service

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.logic.OrderAggregateState

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.util.*

@Service
class OrderService(
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
) {

    fun createOrder(order: CreateOrderRequest): ResponseEntity<Any> {
        for (orderCatalogItem in order.itemsMap) {
            if (orderCatalogItem.value == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attempt to create order with 0 items.")
            }
            val catalogItem = catalogEsService.getState(orderCatalogItem.key)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order item does not exist.")
            if (orderCatalogItem.value > catalogItem.amount) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Order item amount is greater than existing item amount in warehouse.")
            }
        }
        for (orderCatalogItem in order.itemsMap) {
            catalogEsService.create { catalogState ->
                catalogState.updateCatalogItemAmount(
                    orderCatalogItem.key,
                    orderCatalogItem.value, false
                )
            }
        }
        return ResponseEntity(
            orderEsService.create { orderState: OrderAggregateState -> orderState.createOrder(order) },
            HttpStatus.CREATED
        )
    }

    fun addProductToOrder(amount: Int, order_id: UUID, item_id: UUID): ResponseEntity<out Any> {
        val order = orderEsService.getState(order_id) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Order does not exist.")
        val catalogItem = catalogEsService.getState(item_id) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Item does not exist.")
        if (amount == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Attempt to update order with 0 items.")
        }
        if (catalogItem.amount < amount) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Order item amount is greater than existing item amount in warehouse.")
        }
        // decrease item amount
        catalogEsService.create { catalogState ->
            catalogState.updateCatalogItemAmount(
                catalogItem.getId(), amount, false
            )
        }
        return ResponseEntity(
            orderEsService.create { orderState -> orderState.updateOrder(order_id, mapOf(item_id to amount)) },
            HttpStatus.OK
        )
    }
}