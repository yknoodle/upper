package com.noodle.upper.controllers

import com.noodle.upper.services.ListService
import com.noodle.upper.utility.FilePartHelper
import com.noodle.upper.services.SearchService
import com.noodle.upper.services.UploadService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/search")
class SearchController(@Autowired val searchService: SearchService){
    @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun search(@RequestBody strings: Array<String>) = searchService.search(strings)
    @PostMapping("/space-delimited", consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun searchSpaceDelimited(@RequestBody string: String) = searchService.search(string)
}