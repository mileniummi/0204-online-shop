package com.itmo.microservices.demo.booking.api.controller

import com.itmo.microservices.demo.booking.api.dto.BookingLogRecordDto
import com.itmo.microservices.demo.booking.api.service.BookingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class BookingController (
    private val bookingService: BookingService,
) {
    @GetMapping("_internal/bookingHistory/{bookingId}")
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
    fun bookOrder(@PathVariable bookingId: UUID): List<BookingLogRecordDto> = bookingService.getBookingHistory(bookingId)
}