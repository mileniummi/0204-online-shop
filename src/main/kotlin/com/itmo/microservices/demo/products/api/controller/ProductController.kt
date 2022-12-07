package com.itmo.microservices.demo.products.api.controller

import com.itmo.microservices.demo.products.imp.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/items")
class ProductController(
    val productService: ProductService
) {

    @GetMapping
    @Operation(
        summary = "Get products",
        responses = [
            ApiResponse(description = "OK", responseCode = "200"),
            ApiResponse(description = "Products not found", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Unauthorized", responseCode = "403", content = [Content()])
        ],
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    fun getProducts(@RequestParam available: Boolean, @RequestParam size: Int) =
        productService.getProducts(size, available)

}