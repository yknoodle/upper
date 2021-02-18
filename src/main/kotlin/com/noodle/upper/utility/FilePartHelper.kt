package com.noodle.upper.utility

import com.noodle.upper.models.InvoiceCsv
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart

object FilePartHelper {
    @FlowPreview
    fun parse(filePart: FilePart): Flow<InvoiceCsv> {
        return DataBufferUtils
                .join(filePart.content()).asFlow()
                .map{it}
                .map{it.asInputStream()}
                .map(CsvReaderHelper::getCSVReader)
                .flatMapConcat { it.parse().asFlow() }
    }
    @FlowPreview
    fun iterator(filePart: FilePart): Flow<InvoiceCsv> {
        return DataBufferUtils
                .join(filePart.content()).asFlow()
                .map{it.asInputStream()}
                .map{ CsvReaderHelper.getCSVReader(it).iterator()}
                .flatMapConcat { it.asFlow() }
    }
}