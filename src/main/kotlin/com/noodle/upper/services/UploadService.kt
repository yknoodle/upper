package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.InvoiceCsv
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FilePartHelper
import com.noodle.upper.utility.FlowHelper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.ServerSentEvent
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component

@Component
class UploadService(@Autowired val invoiceRepository: ReactiveInvoiceRepository) {
    @FlowPreview
    fun upload(filePart: FilePart): Flow<ServerSentEvent<out Any>> = flow {
        val filename = filePart.filename()
        emit(ServerSentEvent.builder<Int>().comment("begin uploading $filename").build())
        var parsed = 0
        val invoiceCsvFlow: Flow<InvoiceCsv> = FilePartHelper
                .iterator(filePart)
                .onEach{
                    parsed += 1
                    if (parsed%50000==0) emit(ServerSentEvent.builder<Int>().comment("$parsed").id("parseProgress").build())
                }
        val invoiceCsvs: List<Invoice> = invoiceCsvFlow.map {
            Invoice( it.invoiceNo, it.stockCode, it.description, it.quantity, it.invoiceDate, it.unitPrice, it.customerId, it.country )
        }.toList()
        emit(ServerSentEvent.builder<Int>().comment("collected $parsed invoices").build())
        val count = invoiceCsvs.size
        FlowHelper.progress({invoiceRepository.saveAll(invoiceCsvs).asFlow()}, count).onEach{emit(it)}.collect()
    }
}