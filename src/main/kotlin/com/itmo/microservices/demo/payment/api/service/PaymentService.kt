package com.itmo.microservices.demo.payment.api.service

import com.itmo.microservices.demo.catalog.api.service.CatalogItemService
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.payment.api.dto.FinancialOperationType
import com.itmo.microservices.demo.payment.api.dto.PaymentSubmissionDto
import com.itmo.microservices.demo.payment.api.dto.UserAccountFinancialLogRecordDto
import com.itmo.microservices.demo.payment.api.model.PaymentStatus
import com.itmo.microservices.demo.payment.api.model.PaymentSuccessEvent
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import java.math.BigDecimal
import java.util.*

@Service
class PaymentService(
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
    private val catalogItemService: CatalogItemService
) {
    fun getOrderFinLog(orderId: UUID?, userDetails: UserDetails): ResponseEntity<List<UserAccountFinancialLogRecordDto>> {
        val orders: List<OrderAggregateState> = if (orderId != null) {
            val order = orderEsService.getState(orderId)
            if (order == null) {
                throw NotFoundException("Order does not exist.")
            } else {
                listOf(order)
            }
        } else {
            TODO("add orders selects from orders")
        }
        return ResponseEntity(
            orders.map { orderStateToFinanceLog(it) },
            HttpStatus.OK
        )
    }
    private fun orderStateToFinanceLog(state: OrderAggregateState): UserAccountFinancialLogRecordDto {
        val lastPaymentLog = state.paymentHistory.last()
        return UserAccountFinancialLogRecordDto(
            type = FinancialOperationType.WITHDRAW, // TODO: add refunds
            amount = lastPaymentLog.amount,
            orderId = state.getId(),
            timestamp = lastPaymentLog.timestamp,
            paymentTransactionId = lastPaymentLog.transactionId,
        )
    }

    fun processOrderPayment(orderId: UUID, payment: PaymentSubmissionDto): ResponseEntity<PaymentSuccessEvent> {
        val order = orderEsService.getState(orderId) ?: throw NotFoundException("Order does not exist.")

        val items = order.itemsMap
        var totalPrice = BigDecimal(0)
        for (item in items) {
            val catalogItem = catalogItemService.getCatalogItemById(item.key)
            if (catalogItem.isEmpty) {
                throw NotFoundException("Order item does not exist.")
            }
            totalPrice += catalogItem.get().product.price * BigDecimal(item.value)
        }
        val ev = PaymentSuccessEvent(
            amount = totalPrice,
            orderId = orderId,
            status = PaymentStatus.SUCCESS,
            timestamp = payment.timestamp,
            transactionId = payment.transactionId
        )
        orderEsService.create { state -> state.addPayment(ev) }
        return ResponseEntity(ev, HttpStatus.OK)
    }

}