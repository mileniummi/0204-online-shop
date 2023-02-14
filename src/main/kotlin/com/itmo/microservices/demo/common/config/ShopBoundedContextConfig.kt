package com.itmo.microservices.demo.common.config

import com.itmo.microservices.demo.catalog.api.model.CatalogAggregate
import com.itmo.microservices.demo.catalog.logic.CatalogAggregateState
import com.itmo.microservices.demo.orders.api.model.OrderAggregate
import com.itmo.microservices.demo.orders.logic.OrderAggregateState
import com.itmo.microservices.demo.products.api.model.ProductAggregate
import com.itmo.microservices.demo.products.logic.ProductAggregateState
import com.itmo.microservices.demo.users.api.model.UserAggregate
import com.itmo.microservices.demo.users.logic.UserAggregateState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.quipy.core.AggregateRegistry
import ru.quipy.core.EventSourcingServiceFactory
import java.util.*

@Configuration
class ShopBoundedContextConfig {
    @Autowired
    private lateinit var eventSourcingServiceFactory: EventSourcingServiceFactory

    @Autowired
    private lateinit var aggregateRegistry: AggregateRegistry

    @Bean
    fun userEsService() =
        eventSourcingServiceFactory.create<UUID, UserAggregate, UserAggregateState>()
    @Bean
    fun productEsService() =
        eventSourcingServiceFactory.create<UUID, ProductAggregate, ProductAggregateState>()

    @Bean
    fun catalogEsService() =
        eventSourcingServiceFactory.create<UUID, CatalogAggregate, CatalogAggregateState>()

    @Bean
    fun orderEsService() =
        eventSourcingServiceFactory.create<UUID, OrderAggregate, OrderAggregateState>()
}