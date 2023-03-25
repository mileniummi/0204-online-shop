package com.itmo.microservices.demo.products.api.service

import com.itmo.microservices.demo.catalog.projections.CatalogItem
import com.itmo.microservices.demo.catalog.projections.CatalogItemCacheRepository
import com.itmo.microservices.demo.products.api.dto.CatalogItemResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class ProductService(
    private val catalogItemCacheRepository: CatalogItemCacheRepository
) {

    fun getProducts(available: Boolean, size: Int): ResponseEntity<List<CatalogItemResponse>> {
        val items = if (available){
            catalogItemCacheRepository.findAllWithExistingAmount(Pageable.ofSize(size)).content
        } else {
            catalogItemCacheRepository.findAll(Pageable.ofSize(size)).content
        }
        return ResponseEntity.ok(items.map { CatalogItemResponse(
            id = it.id,
            title = it.product.productName,
            description = it.product.description,
            price = it.product.price,
            amount = it.amount,
        ) })
    }
}