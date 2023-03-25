package com.itmo.microservices.demo.payment.api.service

import com.itmo.microservices.demo.catalog.api.service.CatalogItemService
import com.itmo.microservices.demo.common.exceptions.NotFoundException
import com.itmo.microservices.demo.external.TpsExternalService
import com.itmo.microservices.demo.external.callbacks.dto.CallbackStatus
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackAggregate
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackCreatedEvent
import com.itmo.microservices.demo.external.tps.TpsExternalService
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.payment.api.dto.FinancialOperationType
import com.itmo.microservices.demo.payment.api.dto.PaymentSubmissionDto
import com.itmo.microservices.demo.payment.api.dto.UserAccountFinancialLogRecordDto
import com.itmo.microservices.demo.payment.api.model.PaymentStatus
import com.itmo.microservices.demo.payment.api.model.PaymentSuccessEvent
import com.itmo.microservices.demo.utils.ConditionAwaiter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.quipy.core.EventSourcingService
import ru.quipy.streams.AggregateSubscriptionsManager
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class PaymentService(
    private val orderEsService: EventSourcingService<UUID, OrderAggregate, OrderAggregateState>,
    private val catalogItemService: CatalogItemService,
    private val tpsExternalService: TpsExternalService,
    private val catalogItemService: CatalogItemService,
    private val subscriptionsManager: AggregateSubscriptionsManager,
) {
    fun getOrderFinLog(
        orderId: UUID?,
        userDetails: UserDetails
    ): ResponseEntity<List<UserAccountFinancialLogRecordDto>> {
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

    fun processOrderPayment(orderId: UUID): ResponseEntity<PaymentSubmissionDto> {
        val order = orderEsService.getState(orderId) ?: throw NotFoundException("Order does not exist.")
        println("process payment ${order.getStatus()}")

        val items = order.itemsMap
        var totalPrice = BigDecimal(0)
        for (item in items) {
            val catalogItem = catalogItemService.getCatalogItemById(item.key)
            if (catalogItem.isEmpty) {
                throw NotFoundException("Order item does not exist.")
            }
            totalPrice += catalogItem.get().product.price * BigDecimal(item.value)
        }

        val tpsResp = runBlocking { tpsExternalService.createPayment(totalPrice.toDouble()) }
        println(tpsResp)
        val mutex = Mutex(true)
        subscriptionsManager.createSubscriber(
            ExternalServiceCallbackAggregate::class, "::callback-subscriber-${payment.id}"
        ) {
            `when`(ExternalServiceCallbackCreatedEvent::class) { event ->
                if (event.transactionId == payment.id) {
                    status = event.status
                    mutex.unlock()
                }
            }
        }
        ConditionAwaiter.awaitAtMost(10, TimeUnit.SECONDS).condition {
            mutex.tryLock()
        }.onSuccess {
            if (status !== CallbackStatus.PENDING) {
                val ev = PaymentSuccessEvent(
                    amount = totalPrice,
                    orderId = orderId,
                    status = if (status == CallbackStatus.SUCCESS) PaymentStatus.SUCCESS else PaymentStatus.FAILED,
                    timestamp = Instant.now(),
                    transactionId = payment.id
                )
                orderEsService.update(order.getId()) { state -> state.addPayment(ev) }
            }
        }
            .onFailure {
                throw ExternalServiceException(it?.message)
            }.startWaiting()

        subscriptionsManager.destroy()
        val time = Instant.now().toEpochMilli()
        val ev = PaymentSuccessEvent(
            amount = totalPrice,
            orderId = orderId,
            status = PaymentStatus.SUCCESS,
            timestamp = time,
            transactionId = tpsResp.id
        )
        orderEsService.update(orderId) { state -> state.addPayment(ev) }
        orderEsService.update(orderId) { state -> state.completeOrder(orderId) }
        val resp = PaymentSubmissionDto(
            timestamp = time,
            transactionId = tpsResp.id
        )
        return ResponseEntity(resp, HttpStatus.OK)
    }

}