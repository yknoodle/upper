package com.noodle.upper.utility

import com.opencsv.CSVReader
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.exceptions.CsvException
import java.io.InputStream
import java.io.InputStreamReader

object CsvReaderHelper {
    inline fun <reified T> getCSVReader(
            inputStream: InputStream,
            noinline handle: ((e: CsvException) -> CsvException)? = {
                println(it)
                it
            }): CsvToBean<T> {
        return CsvToBeanBuilder<T>(CSVReader(InputStreamReader(inputStream)))
                .withExceptionHandler(handle)
                .withType(T::class.java).build()
    }

    inline fun <reified T> InputStream.asCsvToBean(
            noinline handle: ((e: CsvException) -> CsvException)?): CsvToBean<T> =
            getCSVReader(this, handle)
}