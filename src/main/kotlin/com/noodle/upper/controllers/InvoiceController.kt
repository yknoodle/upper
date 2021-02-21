package com.noodle.upper.controllers

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.services.ListService
import com.noodle.upper.services.SearchService
import com.noodle.upper.services.UploadService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/invoice")
class InvoiceController(
        @Autowired val listService: ListService,
        @Autowired val uploadService: UploadService,
        @Autowired val searchService: SearchService
) {
    @FlowPreview
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
            @RequestPart("invoiceCsv") invoiceCsv: FilePart):
            Flow<ServerSentEvent<Map<String, Float>>> =
            uploadService.upload(invoiceCsv)
                    .onEach{println(it)}
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping
    fun list(
            @RequestParam("pageNumber") pageNumber: Int,
            @RequestParam("pageSize") pageSize: Int):
            Flow<ServerSentEvent<Tracked<Invoice>>> =
            listService.list(pageNumber, pageSize)
                    .onEach{println(it)}
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping(consumes = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun search(
            @RequestParam searchKeys: String):
            Flow<ServerSentEvent<Tracked<List<Invoice>>>> =
            searchService.searchChunked(searchKeys)
                    .catch{println(it)}
                    .onEach{println(it)}
    @FlowPreview
    @PostMapping("/async", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAsync(
            @RequestPart("invoiceCsv") invoiceCsv: FilePart): String =
            uploadService.uploadDbCached(invoiceCsv)
    @GetMapping("/async")
    suspend fun uploadCompletion(@RequestParam("cacheUUID") cacheUUID: String): Map<String, Int> =
            uploadService.uploadProgress(cacheUUID).single()
}