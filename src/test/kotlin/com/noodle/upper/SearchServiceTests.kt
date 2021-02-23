package com.noodle.upper

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.services.SearchService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SearchServiceTests(
        @Autowired val invoiceRepository: ReactiveInvoiceRepository,
        @Autowired val searchService: SearchService
) {
    @Test
    fun contextLoads(){

    }
    @BeforeEach
    fun beforeEach(){
        invoiceRepository.deleteAll().block()
    }
    @FlowPreview
    @ExperimentalCoroutinesApi
    @Test
    fun canSearchPhrase() = runBlocking {
        invoiceRepository.insert(listOf(
                Invoice(uploadId = "upload2", description = "dog"),
                Invoice(uploadId = "upload3", description = "cat pig")
        )).asFlow().toList()
        val invoices: Tracked<List<Invoice>>? = searchService
                .searchPhrase("cat pig").onEach{println(it)}.singleOrNull()
        assert(invoices!=null )
    }
    @FlowPreview
    @ExperimentalCoroutinesApi
    @Test
    fun noSuchPhrase() = runBlocking {
        invoiceRepository.insert(listOf(
                Invoice(uploadId = "upload1", description = "cat"),
                Invoice(uploadId = "upload2", description = "dog"),
        )).asFlow().toList()
        val invoices: Tracked<List<Invoice>>? = searchService
                .searchPhrase("cat pig").onEach{println(it)}.singleOrNull()
        assert(invoices==null )
    }
}