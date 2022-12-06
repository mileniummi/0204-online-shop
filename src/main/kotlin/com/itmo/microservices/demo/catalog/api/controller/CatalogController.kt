package com.itmo.microservices.demo.catalog.api.controller

import com.itmo.microservices.demo.catalog.api.model.AddCatalogItemRequest
import com.itmo.microservices.demo.catalog.logic.service.CatalogTestService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalog")
class CatalogController(
    val catalogService: CatalogTestService
) {

    @PostMapping("/")
    fun getCatalog(@RequestBody request: AddCatalogItemRequest) =
        catalogService.addProduct(request)
}