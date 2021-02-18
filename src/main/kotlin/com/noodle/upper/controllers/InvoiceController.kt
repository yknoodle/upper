package com.noodle.upper.controllers

import com.noodle.upper.services.ListService
import com.noodle.upper.services.SearchService
import com.noodle.upper.services.UploadService
import com.noodle.upper.utility.FilePartHelper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/invoice")
class InvoiceController(
        @Autowired val listService: ListService,
        @Autowired val uploadService: UploadService,
        @Autowired val searchService: SearchService
        ) {
        @GetMapping("/{page}/{size}")
        fun list(@PathVariable("page") page: Int,
                 @PathVariable("size") size: Int) = listService.list(page, size)
        @FlowPreview
        @PostMapping("/parse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
        fun parse(@RequestPart("file") file: FilePart) = FilePartHelper.iterator(file)
                .map{ JSONObject(it).toString()}
                .onEach{println(it)}
        @FlowPreview
        @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
        fun csv(@RequestPart("file") file: FilePart) = uploadService.upload(file)
        @PostMapping("/search", consumes = [MediaType.APPLICATION_JSON_VALUE])
        fun search(@RequestBody strings: Array<String>) = searchService.search(strings)
        @PostMapping("/search/space-delimited", consumes = [MediaType.TEXT_PLAIN_VALUE])
        fun searchSpaceDelimited(@RequestBody string: String) = searchService.search(string)
}