package com.itmo.microservices.demo.booking.api.dto

import java.util.*

class BookingDto (
    id: UUID,
    failedItems: Set<UUID>
)