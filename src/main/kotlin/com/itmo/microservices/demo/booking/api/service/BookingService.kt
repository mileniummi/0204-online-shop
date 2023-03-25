package com.itmo.microservices.demo.booking.api.service

import com.itmo.microservices.demo.booking.api.dto.BookingDto
import com.itmo.microservices.demo.booking.api.dto.BookingLogRecordDto
import com.itmo.microservices.demo.booking.api.model.BookingAggregate
import com.itmo.microservices.demo.booking.api.model.BookingStatus
import com.itmo.microservices.demo.booking.logic.BookingAggregateState
import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.common.exceptions.EntityViolationException
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.orders.api.model.OrderUpdateAfterBooking
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.util.*

@Service
class BookingService(
    private val bookingEsService: EventSourcingService<UUID, BookingAggregate, BookingAggregateState>,
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,

    ) {

    fun processOrderBooking(orderId: UUID): ResponseEntity<BookingDto> {
        val order = orderEsService.getState(orderId) ?: throw NotFoundException("Order does not exist.")
        val items = order.itemsMap
        val itemsToTake = mutableMapOf<UUID, Int>()

        val booking =
            bookingEsService.create { bookingState: BookingAggregateState -> bookingState.createBookingItem() }

        val bookingAggregateState =
            bookingEsService.getState(booking.bookingId) ?: throw NotFoundException("Booking not created")
        println("Start booking\n order = $order \n items = $items")
        for (item in items) {
            println("serving $item")
            val catalogItem = catalogEsService.getState(item.key) ?: throw NotFoundException("Item does not exist.")
            var status = BookingStatus.SUCCESS
            if (catalogItem.amount < item.value) {
                status = BookingStatus.FAILED
            }
            bookingEsService.update(bookingAggregateState.getId()) { bookingState: BookingAggregateState ->
                bookingState.createBookingLog(
                    catalogItem.getId(),
                    item.value,
                    status
                )
            }
            println("$item: $status")
            if (status == BookingStatus.SUCCESS) {
                itemsToTake[item.key] = item.value
            }
        }
        for (orderCatalogItem in itemsToTake) {
            catalogEsService.update(orderCatalogItem.key) { catalogState ->
                catalogState.updateCatalogItemAmount(
                    orderCatalogItem.key,
                    orderCatalogItem.value, false
                )
            }
        }
        if (itemsToTake.isEmpty()){
            throw EntityViolationException("No items left to be reserved")
        }

        val failed = bookingAggregateState.getFailedBookingLogs().map { it.bookingLogId }
        if(failed.isEmpty()) {
            val ev = orderEsService.update(orderId) { orderState ->
                orderState.updateOrderAfterBooking(orderId, OrderStatus.BOOKED, booking.bookingId)
            }
            println("update order after booking")
        } else {
            orderEsService.update(orderId) { orderState ->
                orderState.updateOrderAfterBooking(orderId, OrderStatus.DISCARD, booking.bookingId)
            }
        }
        return ResponseEntity(
            BookingDto(booking.bookingId, failed),
            HttpStatus.ACCEPTED
        )
    }

    fun getBookingHistory(bookingId: UUID): List<BookingLogRecordDto> {
        val booking = bookingEsService.getState(bookingId) ?: throw NotFoundException("No booking $bookingId")
        return booking.getBookingLogs().map {
            BookingLogRecordDto(
                bookingId = bookingId,
                itemId = it.itemId,
                status = it.status,
                amount = it.amount,
                timestamp = it.timestamp,
            )
        }
    }
}