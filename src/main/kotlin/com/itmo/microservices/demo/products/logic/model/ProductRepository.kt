package com.itmo.microservices.demo.products.logic.model

import com.itmo.microservices.demo.products.logic.Product
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

public interface ProductRepository : MongoRepository<Product, UUID>{
}