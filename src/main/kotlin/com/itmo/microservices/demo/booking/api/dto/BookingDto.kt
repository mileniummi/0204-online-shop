package com.itmo.microservices.demo.booking.api.dto

import com.itmo.microservices.demo.booking.api.model.BookingStatus
import java.util.*

data class BookingDto (
    val id: UUID,
    val failedItems: List<UUID>
)

data class BookingLogRecordDto (
    val bookingId: UUID,
    val itemId: UUID,
    val status: BookingStatus?,
    val amount: Int,
    val timestamp: Long
)