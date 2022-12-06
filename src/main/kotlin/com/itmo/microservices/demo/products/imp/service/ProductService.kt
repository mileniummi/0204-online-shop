package com.itmo.microservices.demo.products.imp.service

import com.itmo.microservices.demo.catalog.logic.CatalogItem
import com.itmo.microservices.demo.catalog.logic.model.CatalogRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.data.domain.Page


@Service
class ProductService(
    val catalogRepository: CatalogRepository
) {
    fun getProducts(size: Int, available: Boolean): Page<CatalogItem> {
        return catalogRepository.findAll(Pageable.ofSize(size))
    }

}
