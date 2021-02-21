package com.noodle.upper.controllers

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.services.ListService
import com.noodle.upper.services.SearchService
import com.noodle.upper.services.UploadService
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
    @PostMapping("/async", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAsync(
            @RequestPart("invoiceCsv") invoiceCsv: FilePart): String =
            uploadService.uploadDbCached(invoiceCsv)
    @GetMapping("/async")
    suspend fun uploadCompletion(@RequestParam("uuid") cacheUUID: String): Map<String, Int> =
            uploadService.uploadProgress(cacheUUID).single()
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/{pageNumber}")
    fun list(
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
    suspend fun searchAll(
            @RequestParam("searchTerm") words: String):
            Flow<ServerSentEvent<Tracked<List<Invoice>>>> =
            searchService.searchPhrase(words)
                    .onEach{println("sending ${it.entity?.size}")}
                    .map{ServerSentEvent.builder(it).build()}
                    .catch{println(it)}
    @CrossOrigin(origins = ["http://localhost:3000"])
    @Operation(
            summary = "Old search restful endpoint",
            description = "queries the db using the given string using matchingAny criteria",
            deprecated = true
    )
    @Deprecated("Error prone", ReplaceWith("searchService.searchWords(words).map { ServerSentEvent.builder(it).build() }.catch { println(it) }", "kotlinx.coroutines.flow.map", "org.springframework.http.codec.ServerSentEvent", "kotlinx.coroutines.flow.catch"))
    @GetMapping("/words")
    suspend fun searchString(
            @RequestParam words: String):
            Flow<ServerSentEvent<Tracked<List<Invoice>>>> =
            searchService.searchWords(words)
                    .map{ServerSentEvent.builder(it).build()}
                    .catch{println(it)}
    @FlowPreview
    @Operation(
            summary = "Old upload restful endpoint",
            deprecated = true
    )
    @Deprecated("Error prone", ReplaceWith("uploadService.upload(invoiceCsv).onEach { println(it) }", "kotlinx.coroutines.flow.onEach"))
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
            @RequestPart("invoiceCsv") invoiceCsv: FilePart):
            Flow<ServerSentEvent<Map<String, Float>>> =
            uploadService.upload(invoiceCsv)
                    .onEach{println(it)}
}