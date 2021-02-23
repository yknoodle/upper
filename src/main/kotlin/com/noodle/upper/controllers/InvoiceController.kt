package com.noodle.upper.controllers

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.SubmissionRequest
import com.noodle.upper.models.SubmissionState
import com.noodle.upper.models.Tracked
import com.noodle.upper.services.ListService
import com.noodle.upper.services.SearchService
import com.noodle.upper.services.UploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    @Operation(summary = "Post an invoice submission to be stored",
            responses = [
                ApiResponse(
                        responseCode = "202",
                        description = "The submission posted",
                        content = [
                            Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = Schema(implementation = SubmissionRequest::class),
                                    examples = [
                                        ExampleObject(
                                                "{\n" +
                                                        "    \"id\": \"d0f76577-20a0-4ed3-ac10-d733758c1182\",\n" +
                                                        "    \"count\": 541909,\n" +
                                                        "    \"complete\": false\n" +
                                                        "}"
                                        )
                                    ])])])
    @PostMapping("/submission", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun creation(
            @RequestPart("invoiceCsv") invoiceCsv: FilePart):
            ResponseEntity<SubmissionRequest> =
            flowOf(uploadService.uploadDbCached(invoiceCsv))
                    .map { ResponseEntity.accepted().body(it) }.first()

    @Operation(summary = "Get the status of an invoice submission",
            responses = [
                ApiResponse(
                        description = "State of the submission posted",
                        responseCode = "200",
                        content = [
                            Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = Schema(implementation = SubmissionState::class),
                                    examples = [
                                        ExampleObject(
                                                "{\n" +
                                                        "    \"done\": 76256,\n" +
                                                        "    \"total\": 541909,\n" +
                                                        "    \"completed\": false\n" +
                                                        "}"
                                        )
                                    ])])])
    @GetMapping("/submission")
    suspend fun progress(@RequestParam("uuid") cacheUUID: String):
            ResponseEntity<SubmissionState> =
            uploadService.uploadProgress(cacheUUID).map { ResponseEntity.ok(it) }.first()

    @Operation(summary = "Get paged invoices",
            responses = [
                ApiResponse(
                        description = "text/event-stream of chunks of invoices, and the load progress, fetched and total",
                        responseCode = "200",
                        content = [
                            Content(
                                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                                    schema = Schema(implementation= Tracked::class,subTypes = [Invoice::class]),
                                    examples = [
                                        ExampleObject(
                                                "data: {\"fetched\":1,\"total\":100,\"entity\":[{\"id\":\"603237e452843b36eee6fa27\",\"invoiceNo\":\"536378\",\"stockCode\":\"84519A\",\"description\":\"TOMATO CHARLIE+LOLA COASTER SET\",\"quantity\":6,\"invoiceDate\":\"12/1/2010 9:37\",\"unitPrice\":\"2.95\",\"customerId\":\"14688\",\"country\":\"United Kingdom\",\"score\":0.0,\"uploadId\":\"87a671ae-5bb4-4278-866e-a86b640536da\"}]}"
                                        )
                                    ])])])
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping("/{pageNumber}")
    fun pages(
            @PathVariable("pageNumber") pageNumber: Int,
            @RequestParam("pageSize") pageSize: Int):
            Flow<ServerSentEvent<Tracked<List<Invoice>>>> =
            listService.listChunked(pageNumber, pageSize)
                    .onEach { println("sending ${it.entity?.size}") }
                    .map { ServerSentEvent.builder(it).build() }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @CrossOrigin(origins = ["http://localhost:3000"])
    @GetMapping
    suspend fun search(
            @RequestParam("searchTerm") words: String):
            Flow<ServerSentEvent<Tracked<List<Invoice>>>> =
            searchService.searchPhrase(words)
                    .onEach { println("sending ${it.entity?.size}") }
                    .map { ServerSentEvent.builder(it).build() }
                    .catch { println(it) }
}