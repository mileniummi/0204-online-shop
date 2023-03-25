package com.itmo.microservices.demo.delivery.api.controller

import com.itmo.microservices.demo.delivery.api.service.DeliveryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant


@RestController
@RequestMapping("delivery")
class DeliveriesController(
    private val deliveryService: DeliveryService,
) {

    @Operation(
        summary = "Get slots",
        responses = [
            ApiResponse(description = "OK", responseCode = "200",),
            ApiResponse(description = "Unauthorized", responseCode = "403")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/slots")
    fun getSlots(@RequestParam amount: Int): List<Instant> {
        return deliveryService.getSlots(amount)
    }
}