package com.itmo.microservices.demo.catalog.api.controller

import com.itmo.microservices.demo.catalog.api.dto.AddCatalogItemRequest
import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.api.model.CatalogItemCreatedEvent
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.products.api.model.Product
import com.itmo.microservices.demo.products.api.model.ProductAggregate
import com.itmo.microservices.demo.products.logic.ProductAggregateState
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.quipy.core.EventSourcingService
import java.util.UUID

@RestController
@RequestMapping("/catalog")
class CatalogController(
    private val catalogEsService: EventSourcingService<UUID, CatalogAggregate, CatalogAggregateState>,
    private var productEsService: EventSourcingService<UUID, ProductAggregate, ProductAggregateState>,

    ) {

    @PostMapping("/")
    @Operation(
        summary = "Add product to catalog with amount",
        responses = [
            ApiResponse(description = "OK", responseCode = "201"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun addProduct(@RequestBody request: AddCatalogItemRequest): ResponseEntity<CatalogItemCreatedEvent> {
        val productCreatedEvent = productEsService.create { product: ProductAggregateState ->
            product.createNewProduct(
                request.name,
                request.price,
                request.description
            )
        }
        return ResponseEntity(catalogEsService.create { catalog ->
            catalog.createCatalogItem(
                Product(
                    productCreatedEvent.productId,
                    productCreatedEvent.productName,
                    productCreatedEvent.price,
                    productCreatedEvent.description
                ), request.amount
            )
        }, HttpStatus.CREATED)
    }
}