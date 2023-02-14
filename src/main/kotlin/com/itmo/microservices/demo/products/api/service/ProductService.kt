package com.itmo.microservices.demo.products.api.service

import com.itmo.microservices.demo.catalog.projections.CatalogItem
import com.itmo.microservices.demo.catalog.projections.CatalogItemCacheRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class ProductService(
    private val catalogItemCacheRepository: CatalogItemCacheRepository
) {

    fun getProducts(available: Boolean, size: Int): ResponseEntity<Page<CatalogItem>> {
        if (available){
            return ResponseEntity.ok(catalogItemCacheRepository.findAllWithExistingAmount(Pageable.ofSize(size)))
        }
        return ResponseEntity.ok(catalogItemCacheRepository.findAll(Pageable.ofSize(size)))
    }
}