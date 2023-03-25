package com.itmo.microservices.demo.orders.api.controller

import com.itmo.microservices.demo.booking.api.service.BookingService
import com.itmo.microservices.demo.catalog.api.service.CatalogItemService
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.dto.OrderItem
import com.itmo.microservices.demo.orders.api.dto.OrderResponse
import com.itmo.microservices.demo.orders.api.service.OrderService
import com.itmo.microservices.demo.orders.projections.OrderCache
import com.itmo.microservices.demo.orders.projections.OrderRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("orders")
class OrdersController(
    private val orderRepository: OrderRepository,
    private val catalogService: CatalogItemService,
    private val orderService: OrderService,
    private val bookingService: BookingService,
) {

    @GetMapping("/{order_id}")
    @Operation(
        summary = "Get order",
        responses = [
            ApiResponse(
                description = "OK",
                responseCode = "200",
            ),
            ApiResponse(description = "Order not found", responseCode = "404"),
            ApiResponse(description = "Unauthorized", responseCode = "403")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getOrder(@PathVariable order_id: UUID): ResponseEntity<OrderResponse> {
        println()
        val c = orderRepository.findById(order_id).orElseThrow { NotFoundException("No order $order_id") }
//        val items: MutableMap<OrderItem, Int> = mutableMapOf()
//        for (i in c.itemsMap.entries) {
//            val ci = catalogService.getCatalogItemById(i.key).orElseThrow{NotFoundException("No item $i")}
//            val ci1 = OrderItem(id = ci.id, title = ci.product.productName, price = ci.product.price)
//            items[ci1] = i.value
//        }
        val resp = OrderResponse(
            id = c.id,
            status = c.status,
            timeCreated = c.timeCreated,
            itemsMap = c.itemsMap,
            deliveryDuration =
                if (c.deliverySlot != null ){ Duration.between(Instant.now(), c.deliverySlot) } else null,
            paymentHistory = c.paymentHistory,
        )
        println(resp)
        return ResponseEntity(
            resp,
            HttpStatus.CREATED
        )
    }

    @PostMapping
    @Operation(
        summary = "Create order",
        responses = [
            ApiResponse(
                description = "OK",
                responseCode = "201",
            ),
            ApiResponse(description = "Bad request", responseCode = "400"),
            ApiResponse(description = "Order item does not exist.", responseCode = "404"),
            ApiResponse(
                description = "Order item amount is greater than existing item amount in warehouse",
                responseCode = "409",
            ),
            ApiResponse(description = "Unauthorized", responseCode = "403")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun createOrder(@RequestBody order: CreateOrderRequest?) = orderService.createOrder(order)

    @PutMapping("{order_id}/items/{item_id}")
    @Operation(
        summary = "Add item to order",
        responses = [
            ApiResponse(
                description = "OK",
                responseCode = "200",
            ),
            ApiResponse(description = "Order not found", responseCode = "404"),
            ApiResponse(description = "Unauthorized", responseCode = "403")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun addProductsToOrder(
        @RequestParam amount: Int, @PathVariable order_id: UUID, @PathVariable item_id: UUID,
    ) =
        orderService.addProductToOrder(amount, order_id, item_id)

    @PostMapping("{order_id}/delivery")
    @Operation(
        summary = "Set order delivery slot",
        responses = [
            ApiResponse(
                description = "OK",
                responseCode = "200",
            ),
            ApiResponse(description = "Order not found", responseCode = "404"),
            ApiResponse(description = "Unauthorized", responseCode = "403")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    suspend fun setOrderDeliverySlot(@RequestParam slot: Instant, @PathVariable order_id: UUID) =
        orderService.setOrderDeliverySlot(slot, order_id)

    @PostMapping("{order_id}/bookings")
    @Operation(
        summary = "Book order",
        responses = [
            ApiResponse(
                description = "OK",
                responseCode = "200",
            ),
            ApiResponse(description = "Order not found", responseCode = "404"),
            ApiResponse(description = "Unauthorized", responseCode = "403")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun bookOrder(@PathVariable order_id: UUID) = bookingService.processOrderBooking(order_id)
}