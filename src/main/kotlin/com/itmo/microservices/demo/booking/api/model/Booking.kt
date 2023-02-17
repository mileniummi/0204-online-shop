package com.itmo.microservices.demo.booking.api.model

import java.util.*

class Booking(
    val bookingId: UUID,
    val status: BookingStatus,
    val failedItems: Set<UUID>
)

enum class BookingStatus {FAILED, SUCCESS}