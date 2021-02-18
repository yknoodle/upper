package com.noodle.upper.controllers

import com.noodle.upper.models.Invoice
import com.noodle.upper.services.ListService
import com.noodle.upper.services.SearchService
import com.noodle.upper.services.UploadService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
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
            Flow<ServerSentEvent<out Any>> =
            uploadService.upload(invoiceCsv)
    @GetMapping
    fun list(
            @RequestParam("pageNumber") pageNumber: Int,
            @RequestParam("pageSize") pageSize: Int):
            Flow<ServerSentEvent<Invoice>> =
            listService.list(pageNumber, pageSize)
    @GetMapping(consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun search(
            @RequestParam searchKeys: String):
            Flow<ServerSentEvent<Invoice>> =
            searchService.search(searchKeys)
}