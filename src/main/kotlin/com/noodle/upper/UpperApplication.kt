package com.noodle.upper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@EnableReactiveMongoRepositories
class UpperApplication

fun main(args: Array<String>) {
	runApplication<UpperApplication>(*args)
}
