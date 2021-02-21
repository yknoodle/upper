package com.noodle.upper.models

import org.springframework.data.mongodb.core.mapping.Document

@Document
data class SubmissionRequest(
        val id: String,
        val count: Int,
        val complete: Boolean = false) {
}