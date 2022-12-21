package com.itmo.microservices.demo.catalog.api.service

import com.itmo.microservices.demo.catalog.projections.CatalogItem
import com.itmo.microservices.demo.catalog.projections.CatalogItemCacheRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class CatalogItemService(
    private val catalogItemCacheRepository: CatalogItemCacheRepository
) {

    fun getCatalogItemById(id: UUID): Optional<CatalogItem> {
        return catalogItemCacheRepository.findById(id)
    }
}