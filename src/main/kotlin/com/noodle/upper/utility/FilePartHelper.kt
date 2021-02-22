package com.noodle.upper.utility

import com.opencsv.exceptions.CsvException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.reactive.asFlow
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart

@FlowPreview
inline fun <reified T> FilePart.asFlow(
        noinline handle: ((e: CsvException) -> CsvException)? = { it }): Flow<T> =
        DataBufferUtils.join(this.content()).asFlow()
                .flatMapConcat {
                    it.asInputStream()
                            .asCsvToBean<T>(handle).iterator().asFlow()
                }
