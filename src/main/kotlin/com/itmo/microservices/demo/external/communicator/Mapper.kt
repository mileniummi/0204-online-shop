package com.itmo.microservices.demo.external.communicator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val mapper = jacksonObjectMapper().apply {
    findAndRegisterModules()
}