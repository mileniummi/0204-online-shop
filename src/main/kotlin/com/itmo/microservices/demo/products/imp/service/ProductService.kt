package com.itmo.microservices.demo.products.imp.service

import com.itmo.microservices.demo.catalog.logic.CatalogItem
import com.itmo.microservices.demo.catalog.logic.model.CatalogRepository
import org.springframework.stereotype.Service
import java.util.stream.Collectors


@Service
class ProductService(
    val catalogRepository: CatalogRepository
) {
    fun getProducts(size: Int, available: Boolean): List<CatalogItem> {
        return if (available) {
            val products = catalogRepository.findAllByCountGreaterThan(0)
            products.stream().limit(size.toLong()).collect(Collectors.toList())
        } else {
            val products = catalogRepository.findAllByCountGreaterThan(-1)
            products.stream().limit(size.toLong()).collect(Collectors.toList())
        }
    }

}
