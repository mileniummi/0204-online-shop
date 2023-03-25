package com.itmo.microservices.demo.delivery.api.controller

import com.itmo.microservices.demo.delivery.api.model.DeliveryLog
import com.itmo.microservices.demo.delivery.api.service.DeliveryService
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
//import com.itmo.microservices.demo.external.TpsExternalService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.quipy.core.EventSourcingService
import java.time.Duration
import java.time.Instant
import java.util.UUID


@RestController
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
    @GetMapping("/delivery/slots")
    fun getSlots(@RequestParam number: Int): List<Int> {
        return deliveryService.getSlots(number)
    }

    @Operation(
        summary = "Get delivery history",
        responses = [
            ApiResponse(description = "OK", responseCode = "200",),
            ApiResponse(description = "Unauthorized", responseCode = "403")
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/_internal/deliveryLog/{orderId}")
    fun getDeliveryHistory(@PathVariable orderId: UUID): List<DeliveryLog> {
        return deliveryService.getDeliveryHistory(orderId)
    }
}