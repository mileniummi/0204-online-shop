package com.itmo.microservices.demo.booking.api.model

import java.util.*

class Booking (
    id: UUID,
    bookingLogs: Set<UUID>
)

data class BookingLogRecord (
    val bookingId: UUID,
    val bookingLogId: UUID,
    val itemId: UUID,
    val status: BookingStatus,
    val amount: Int,
    val timestamp: Long
)

enum class BookingStatus {FAILED, SUCCESS}