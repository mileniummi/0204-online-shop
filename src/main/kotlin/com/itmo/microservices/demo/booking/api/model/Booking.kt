package com.itmo.microservices.demo.booking.api.model

import java.util.*

class BookingLogRecord (
    bookingId: UUID,
    itemId: UUID,
    status: BookingStatus?,
    amount: Int,
    timestamp: Long
)

class Booking (
    id: UUID,
    bookingLogs: Set<UUID>
)

enum class BookingStatus {FAILED, SUCCESS}