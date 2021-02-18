package com.noodle.upper.repositories

import com.noodle.upper.models.Invoice
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveInvoiceRepository: ReactiveMongoRepository<Invoice, Long>{
    fun countAllBy(criteria: TextCriteria): Mono<Int>
    fun findAllBy(criteria: TextCriteria): Flux<Invoice>
    fun findAllBy(pageable: Pageable): Flux<Invoice>
}