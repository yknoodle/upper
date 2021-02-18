package com.noodle.upper.models

import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Invoice(
        @TextIndexed
        val invoiceNo: String? = null,
        @TextIndexed
        val stockCode: String? = null,
        @TextIndexed
        val description: String? = null,
        val quantity: Int = 0,
        @TextIndexed
        val invoiceDate: String? = null,
        @TextIndexed
        val unitPrice: String? = null,
        @TextIndexed
        val customerId: String? = null,
        @TextIndexed
        val country: String? = null,
){


}
