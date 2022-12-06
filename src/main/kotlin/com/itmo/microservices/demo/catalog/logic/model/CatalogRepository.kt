package com.itmo.microservices.demo.catalog.logic.model

import com.itmo.microservices.demo.catalog.logic.CatalogItem
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
public interface CatalogRepository : MongoRepository<CatalogItem, UUID> {

}