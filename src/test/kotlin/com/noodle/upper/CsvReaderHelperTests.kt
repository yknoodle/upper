package com.noodle.upper

import com.noodle.upper.models.Invoice
import com.noodle.upper.utility.asCsvToBean
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStream

class CsvReaderHelperTests {
    @Test
    fun canGetCsvToBeanFromInputStream(){
        val csvFile = File("src/test/resources/test.csv")
        val inputStream: InputStream = csvFile.inputStream()
        val entries: Int = inputStream.asCsvToBean<Invoice>{it}.count()
        println(entries)
        assert(entries==5)
    }
}