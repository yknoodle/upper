package com.noodle.upper.utility

import com.noodle.upper.models.InvoiceCsv
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart

object FilePartHelper {
    @FlowPreview
    inline fun <reified T> iterator(filePart: FilePart): Flow<T> {
        return DataBufferUtils
                .join(filePart.content()).asFlow()
                .map{it.asInputStream()}
                .map{ CsvReaderHelper.getCSVReader<T>(it).iterator()}
                .flatMapConcat { it.asFlow() }
    }
    @FlowPreview
    fun FilePart.asFlow(): Flow<InvoiceCsv> = iterator(this)
}