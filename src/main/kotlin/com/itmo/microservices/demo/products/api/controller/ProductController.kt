package com.itmo.microservices.demo.products.api.controller
import com.itmo.microservices.demo.products.imp.service.ProductService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/items")
class ProductController(
    val productService: ProductService
) {

    @GetMapping
    fun getProducts(@RequestParam available: Boolean, @RequestParam size: Int) =
        productService.getProducts(size, available)
}