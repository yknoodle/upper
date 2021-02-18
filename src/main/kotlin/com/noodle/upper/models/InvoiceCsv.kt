package com.noodle.upper.models

import com.opencsv.bean.CsvBindByName
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.TextIndexed
import org.springframework.data.mongodb.core.mapping.Document

class InvoiceCsv() {
        @CsvBindByName(column = "InvoiceNo")
        val invoiceNo: String? = null
        @CsvBindByName(column = "StockCode")
        val stockCode: String? = null
        @CsvBindByName(column = "Description")
        val description: String? = null
        @CsvBindByName(column = "Quantity")
        val quantity: Int = 0
        @CsvBindByName(column="InvoiceDate")
        val invoiceDate: String? = null
        @CsvBindByName(column = "UnitPrice")
        val unitPrice: String? = null
        @CsvBindByName(column = "CustomerID")
        val customerId: String? = null
        @CsvBindByName(column = "Country")
        val country: String? = null

}
