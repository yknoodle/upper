package com.noodle.upper.controllers

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class Controller {
    @Bean
    fun route() = router {
        GET("/route") { _ -> ServerResponse.ok().body(fromValue(arrayOf(1,2,3))) }
    }
}