package com.noodle.upper

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.services.ListService
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ListServiceTests(
        @Autowired val listService: ListService,
        @Autowired val invoiceRepository: ReactiveInvoiceRepository
) {
    @Test
    fun contextLoads(){

    }
    @BeforeEach
    fun beforeEach(){
        invoiceRepository.deleteAll().block()
    }
    @Test
    fun listIsPaged() = runBlocking {
        invoiceRepository.insert(listOf(
                Invoice(uploadId = "upload1", description = "boy"),
                Invoice(uploadId = "upload2", description = "girl"),
                Invoice(uploadId = "upload3", description = "man")
        )).asFlow().toList()
        val trackedPage: Tracked<List<Invoice>>? =
                listService.listChunked(0, 2).singleOrNull()
        assert(trackedPage!=null)
        assert(trackedPage?.fetched == 2)
        assert(trackedPage?.entity?.size==2)
    }
    @Test
    fun pageNumberExceeds() = runBlocking {
        invoiceRepository.insert(listOf(
                Invoice(uploadId = "upload1", description = "boy")
        )).asFlow().toList()
        val trackedPage: Tracked<List<Invoice>>? =
                listService.listChunked(1, 1).singleOrNull()
        assert(trackedPage==null)
    }
    @Test
    fun pageNumberEdge() = runBlocking {
        invoiceRepository.insert(listOf(
                Invoice(uploadId = "upload1", description = "boy"),
                Invoice(uploadId = "upload2", description = "girl"),
                Invoice(uploadId = "upload3", description = "man")
        )).asFlow().toList()
        val trackedPage: Tracked<List<Invoice>>? =
                listService.listChunked(1, 2).singleOrNull()
        println(trackedPage)
        assert(trackedPage!=null)
        assert(trackedPage?.fetched==1)
        assert(trackedPage?.entity?.size==1)
    }
}