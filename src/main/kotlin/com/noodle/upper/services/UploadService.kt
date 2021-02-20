package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.base100
import com.noodle.upper.models.completion
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FilePartHelper.asFlow
import com.noodle.upper.utility.FlowHelper
import com.noodle.upper.utility.FlowHelper.chunked
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
    fun upload(invoices: FilePart): Flow<ServerSentEvent<Map<String, Float>>> = flow {
        val chunkSize = 1000 // make configurable
        val invoiceCsvFlow: Flow<Invoice> = invoices.asFlow().map {
            Invoice(null, it.invoiceNo, it.stockCode, it.description, it.quantity, it.invoiceDate, it.unitPrice, it.customerId, it.country )
        }
        val count = invoiceCsvFlow.toList().size
        val saveFlow: Flow<String> = invoiceCsvFlow
                .chunked(chunkSize)
                .flatMapConcat{invoiceRepository.saveAll(it).asFlow()}
                .map{it.id?:"unknown"}
        FlowHelper.track({saveFlow}, count)
                .distinctUntilChangedBy{it.base100()}
                .onEach{ emit(ServerSentEvent.builder(mapOf("uploaded" to it.completion())).build())}
                .collect()
    }
}