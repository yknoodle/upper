package com.noodle.upper.controllers

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.services.ListService
import com.noodle.upper.services.SearchService
import com.noodle.upper.services.SubmissionState
import com.noodle.upper.services.UploadService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    @PostMapping("/submission", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun creation(
            @RequestPart("invoiceCsv") invoiceCsv: FilePart): String =
            uploadService.uploadDbCached(invoiceCsv)
    @GetMapping("/submission")
    suspend fun progress(@RequestParam("uuid") cacheUUID: String):
            ResponseEntity<SubmissionState> =
        uploadService.uploadProgress(cacheUUID).map{ResponseEntity.ok(it)}.first()
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/{pageNumber}")
    fun pages(
            @PathVariable("pageNumber") pageNumber: Int,
            @RequestParam("pageSize") pageSize: Int):
            Flow<ServerSentEvent<Tracked<List<Invoice>>>> =
            listService.listChunked(pageNumber, pageSize)
                    .onEach{println("sending ${it.entity?.size}")}
                    .map { ServerSentEvent.builder(it).build() }
    @FlowPreview
    @ExperimentalCoroutinesApi
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping
    suspend fun search(
            @RequestParam("searchTerm") words: String):
            Flow<ServerSentEvent<Tracked<List<Invoice>>>> =
            searchService.searchPhrase(words)
                    .onEach{println("sending ${it.entity?.size}")}
                    .map{ServerSentEvent.builder(it).build()}
                    .catch{println(it)}
}