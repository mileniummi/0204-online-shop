package com.itmo.microservices.demo.catalog.logic

import com.itmo.microservices.demo.products.logic.Product
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class CatalogItem(val count: Int, val product: Product) {}

