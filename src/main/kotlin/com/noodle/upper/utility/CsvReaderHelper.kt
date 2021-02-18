package com.noodle.upper.utility

import com.noodle.upper.models.InvoiceCsv
import com.opencsv.CSVReader
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import java.io.InputStream
import java.io.InputStreamReader

object CsvReaderHelper {
    fun getCSVReader(inputStream: InputStream): CsvToBean<InvoiceCsv> {
        return CsvToBeanBuilder<InvoiceCsv>(CSVReader(InputStreamReader(inputStream)))
                .withType(InvoiceCsv::class.java).build()
    }
}