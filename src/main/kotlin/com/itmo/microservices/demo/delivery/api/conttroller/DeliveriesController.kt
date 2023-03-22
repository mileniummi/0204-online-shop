package com.itmo.microservices.demo.delivery.api.conttroller

import com.itmo.microservices.demo.delivery.api.service.DeliveryService
import com.itmo.microservices.demo.external.TpsExternalService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID


@RestController
@RequestMapping("delivery")
class DeliveriesController(
    private val deliveryService: DeliveryService,
    private val tpsExternalService: TpsExternalService,
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
    suspend fun getSlots(@RequestParam amount: Int): List<Instant> {
        // delete when postconstruct implemented
        tpsExternalService.createProject("online-shop")
        // ensure external service transaction was successful
        tpsExternalService.getDeliverySlot(UUID.randomUUID())
        //todo check if was successful
        return deliveryService.getSlots(amount)
    }
}