package com.itmo.microservices.demo.catalog.logic.service

import com.itmo.microservices.demo.catalog.api.model.AddCatalogItemRequest
import com.itmo.microservices.demo.catalog.logic.model.CatalogRepository
import com.itmo.microservices.demo.products.logic.model.ProductRepository
import org.springframework.stereotype.Service

@Service
class CatalogManagerService(
    private val catalogRepository: CatalogRepository,
    private val productRepository: ProductRepository
) {
    fun addProduct(request: AddCatalogItemRequest) {
    }
}