package com.itmo.microservices.demo.orders.api.controller

import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.service.OrderService
import com.itmo.microservices.demo.orders.projections.OrderRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("orders")
class OrdersController(
    private val orderRepository: OrderRepository,
    private val orderService: OrderService,
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
    fun getOrder(@PathVariable order_id: UUID) = orderRepository.findById(order_id)

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
    fun createOrder(@RequestBody order: CreateOrderRequest) = orderService.createOrder(order)

    @PostMapping("{order_id}/items/{item_id}")
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
    fun setOrderDeliverySlot(@RequestParam slot: Instant, @PathVariable order_id: UUID) =
        orderService.setOrderDeliverySlot(slot, order_id)

    @PostMapping("{order_id}/payment")
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
    fun processOrderPayment(@PathVariable order_id: UUID) = orderService.processOrderPayment(order_id)
}