package com.itmo.microservices.demo.external.callbacks.conroller

import com.itmo.microservices.demo.external.callbacks.dto.CallbackResult
import com.itmo.microservices.demo.external.callbacks.logic.ExternalServiceCallbackAggregateState
import com.itmo.microservices.demo.external.callbacks.model.ExternalServiceCallbackAggregate
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.*
import ru.quipy.core.EventSourcingService
import java.util.*

@RestController
@RequestMapping("external/callbacks")
class ExternalServiceCallbackController(
    private val externalServiceCallbackEsService: EventSourcingService<
            UUID, ExternalServiceCallbackAggregate, ExternalServiceCallbackAggregateState>
) {

    @PostMapping
    @Operation(
        summary = "Handle Callback",
        responses = [
            ApiResponse(
                description = "OK",
                responseCode = "200",
            )
        ],
    )
    fun handleCallback(@RequestBody callback: CallbackResult) =
        externalServiceCallbackEsService.create {
            it.createCallback(callback)
        }
}