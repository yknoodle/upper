package com.noodle.upper.utility

import com.opencsv.CSVReader
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import java.io.InputStream
import java.io.InputStreamReader

object CsvReaderHelper {
    inline fun <reified T> getCSVReader(inputStream: InputStream): CsvToBean<T> {
        return CsvToBeanBuilder<T>(CSVReader(InputStreamReader(inputStream)))
                .withType(T::class.java).build()
    }
}