package com.itmo.microservices.demo.common.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class ExternalServiceException(message: String?) : RuntimeException(message)