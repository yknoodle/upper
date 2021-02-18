package com.noodle.upper.controllers

import com.noodle.upper.services.UploadService
import com.noodle.upper.utility.FilePartHelper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/upload")
class UploadController( @Autowired val uploadService: UploadService){
    @FlowPreview
    @PostMapping("/parse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun parse(@RequestPart("file") file: FilePart) = FilePartHelper.iterator(file)
            .map{ JSONObject(it).toString()}
            .onEach{println(it)}
    @FlowPreview
    @PostMapping("/ecommerce", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun csv(@RequestPart("file") file: FilePart) = uploadService.upload(file)
}