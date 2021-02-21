package com.noodle.upper

import io.swagger.v3.oas.annotations.ExternalDocumentation
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@SpringBootApplication
@EnableReactiveMongoRepositories
@OpenAPIDefinition(
		info = Info(
				title = "Upper",
				version = "0.0",
				description = "Invoice upload and query",
				contact = Contact( name = "damien", email = "damien.hoyk@gmail.com")
		),
		externalDocs = ExternalDocumentation(description = "github readme",url = "https://github.com/yknoodle/upper"),
		servers = [
			Server(
					description = "local testing",
					url = "http://localhost:8080",
			),
			Server(
					description = "repl.it",
					url = "http://repl.it/yknoodle/upper",
			)
		]
)
class UpperApplication

fun main(args: Array<String>) {
	runApplication<UpperApplication>(*args)
}
