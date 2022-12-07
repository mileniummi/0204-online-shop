package com.itmo.microservices.demo.catalog.api.controller

import com.itmo.microservices.demo.catalog.api.model.AddCatalogItemRequest
import com.itmo.microservices.demo.catalog.logic.service.CatalogManagerService
import com.itmo.microservices.demo.products.api.model.ProductAggregate
import com.itmo.microservices.demo.products.logic.Product
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.quipy.core.EventSourcingService
import java.util.UUID

@RestController
@RequestMapping("/catalog")
class CatalogController(
    private val catalogService: CatalogManagerService,
    val productEsService: EventSourcingService<UUID, ProductAggregate, Product>
) {
    @PostMapping("/")
    @Operation(
        summary = "Post product",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getCatalog(@RequestBody request: AddCatalogItemRequest) {
        print(request)
        this.catalogService.addProduct(request)
    }
}