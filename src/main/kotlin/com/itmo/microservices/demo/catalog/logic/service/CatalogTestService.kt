package com.itmo.microservices.demo.catalog.logic.service

import com.itmo.microservices.demo.catalog.api.model.AddCatalogItemRequest
import com.itmo.microservices.demo.catalog.logic.CatalogItem
import com.itmo.microservices.demo.catalog.logic.model.CatalogRepository
import com.itmo.microservices.demo.products.logic.Product
import com.itmo.microservices.demo.products.logic.model.ProductRepository
import org.springframework.stereotype.Service

@Service
class CatalogTestService(
    val catalogRepository: CatalogRepository,
    val productRepository: ProductRepository
) {
    fun addProduct(request: AddCatalogItemRequest) {
        val product = productRepository.insert(Product(request.name, request.description, request.price))
        catalogRepository.insert(CatalogItem(request.amount, product))
    }
}