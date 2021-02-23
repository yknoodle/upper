package com.noodle.upper

import com.noodle.upper.models.Invoice
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.query.TextCriteria

/**
 * @author damien
 * Probably not entirely necessary to run repository tests, because they are part of the
 * Spring Data library, and should work as long as Spring is maintained
 */
@SpringBootTest
class RepositoryTests {
    @BeforeEach
    fun beforeEach(
            @Autowired reactiveInvoiceRepository: ReactiveInvoiceRepository) {
        reactiveInvoiceRepository.deleteAll().block()
    }
    @Test
    fun canInsertInvoice(
            @Autowired reactiveInvoiceRepository: ReactiveInvoiceRepository) = runBlocking {
        val inserted = reactiveInvoiceRepository.insert(Invoice()).asFlow().firstOrNull()
        assert(inserted != null)
    }

    @Test
    fun canTextQuery(
            @Autowired reactiveInvoiceRepository: ReactiveInvoiceRepository) = runBlocking {
        val inserted: Invoice? = reactiveInvoiceRepository.insert(Invoice(id = "someId", description = "some Description")).asFlow().firstOrNull()
        val queried: Invoice? = reactiveInvoiceRepository.findAllBy(TextCriteria.forDefaultLanguage().matchingAny("some Description")).asFlow().firstOrNull()
        assert(inserted?.description == queried?.description)
    }

    @Test
    fun noResultQuery(
            @Autowired reactiveInvoiceRepository: ReactiveInvoiceRepository) = runBlocking {
        reactiveInvoiceRepository.insert(Invoice(id = "someId", description = "some Description")).asFlow().first()
        val queried: Invoice? = reactiveInvoiceRepository.findAllBy(TextCriteria.forDefaultLanguage().matchingAny("abc")).asFlow().firstOrNull()
        assert(queried == null)
    }

    @Test
    fun pagedResultQuery(
            @Autowired reactiveInvoiceRepository: ReactiveInvoiceRepository) = runBlocking {
        reactiveInvoiceRepository.insert(listOf(
                Invoice(id = "someId", description = "some Description"),
                Invoice(id = "someId2", description = "some Description2"),
                Invoice(id = "someId3", description = "some Description3"))).asFlow().toList()
        val queriedPage1: List<Invoice> = reactiveInvoiceRepository.findAllBy(PageRequest.of(0,2)).asFlow().toList()
        val queriedPage2: List<Invoice> = reactiveInvoiceRepository.findAllBy(PageRequest.of(1,2)).asFlow().toList()
        assert(queriedPage1.size == 2)
        assert(queriedPage2.size == 1)
    }
}