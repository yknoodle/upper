package com.noodle.upper.models

import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.TextScore

@Document
data class Invoice(
        val id: String? = null,
        @TextIndexed(weight = 3F)
        val invoiceNo: String? = null,
        @TextIndexed(weight = 2F)
        val stockCode: String? = null,
        @TextIndexed
        val description: String? = null,
        val quantity: Int = 0,
        @TextIndexed
        val invoiceDate: String? = null,
        @TextIndexed
        val unitPrice: String? = null,
        @TextIndexed(weight = 1F)
        val customerId: String? = null,
        @TextIndexed
        val country: String? = null,
        @TextScore
        val score: Float = 0F,
        val uploadId: String? = null
){


}
