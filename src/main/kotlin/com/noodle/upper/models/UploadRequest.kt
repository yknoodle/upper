package com.noodle.upper.models

import org.springframework.data.mongodb.core.mapping.Document

@Document
data class UploadRequest(
        val id: String,
        val count: Int) {
}