package ru.quipy.bankDemo.accounts.config

import com.itmo.microservices.demo.products.api.model.ProductAggregate
import com.itmo.microservices.demo.products.logic.Product
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

import ru.quipy.core.EventSourcingService
import ru.quipy.core.EventSourcingServiceFactory
import java.util.*

@EnableAutoConfiguration
@ComponentScan
@Configuration
class ProductBoundedContextConfig {

    @Autowired
    private lateinit var eventSourcingServiceFactory: EventSourcingServiceFactory

    @Bean
    fun productEsService(): EventSourcingService<UUID, ProductAggregate, Product> =
        eventSourcingServiceFactory.create()
}