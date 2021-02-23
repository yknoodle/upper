package com.noodle.upper.utility

import com.opencsv.CSVReader
import com.opencsv.bean.CsvToBean
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.exceptions.CsvException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.reactive.asFlow
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import java.io.InputStream
import java.io.InputStreamReader

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

@FlowPreview
inline fun <reified T> FilePart.asFlow(
        noinline handle: ((e: CsvException) -> CsvException)? = { it }): Flow<T> =
        DataBufferUtils.join(this.content()).asFlow()
                .flatMapConcat { it.asInputStream().asCsvToBean<T>(handle).iterator().asFlow() }
