package com.itmo.microservices.demo.payment.api.controller
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.orders.api.dto.CreateOrderRequest
import com.itmo.microservices.demo.orders.api.service.OrderService
import com.itmo.microservices.demo.orders.projections.OrderCache
import com.itmo.microservices.demo.orders.projections.OrderRepository
import com.itmo.microservices.demo.payment.api.dto.FinancialOperationType
import com.itmo.microservices.demo.payment.api.dto.PaymentSubmissionDto
import com.itmo.microservices.demo.payment.api.dto.UserAccountFinancialLogRecordDto
import com.itmo.microservices.demo.payment.api.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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
class PaymentController (
    private val paymentService: PaymentService
) {
    @PostMapping("/order/{orderId}/payment")
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
    fun processOrderPayment(@PathVariable orderId: UUID, @RequestBody payment: PaymentSubmissionDto)
        = paymentService.processOrderPayment(orderId, payment)

    @GetMapping("/finlog")
    @Operation(
        summary = "Get order fin log",
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
    fun getOrderFinLog(
        @RequestParam(required = false) orderId: UUID?,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<List<UserAccountFinancialLogRecordDto>> = paymentService.getOrderFinLog(orderId, user)
}
