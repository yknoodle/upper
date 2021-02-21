package com.noodle.upper.services

import com.noodle.upper.models.*
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.repositories.UploadRequestRepository
import com.noodle.upper.services.Loader.load
import com.noodle.upper.utility.FilePartHelper.asFlow
import com.noodle.upper.utility.FlowHelper
import com.noodle.upper.utility.FlowHelper.hotChunks
import com.noodle.upper.utility.uuid
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.ServerSentEvent
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UploadService(
        @Autowired val invoiceRepository: ReactiveInvoiceRepository,
        @Autowired val uploadRequestRepository: UploadRequestRepository) {
    @FlowPreview
    fun upload(invoices: FilePart): Flow<ServerSentEvent<Map<String, Float>>> = flow {
        val invoiceCsvFlow: Flow<Invoice> = invoices.asFlow<InvoiceCsv>().map { it.asInvoice() }
        val count = invoiceCsvFlow.toList().size.toLong()
        val saveFlow: Flow<String> = invoiceCsvFlow.hotChunks()
                .flatMapConcat { invoiceRepository.insert(it).asFlow() }
                .map { it.id ?: "unknown" }
        FlowHelper.track({ saveFlow }, count)
                .distinctUntilChangedBy { it.base100() }
                .onEach { emit(ServerSentEvent.builder(mapOf("uploaded" to it.completion())).build()) }
                .collect()
    }

    @FlowPreview
    @Transactional
    fun uploadCached(invoices: FilePart): String = load(suspend { invoices.asFlow<Invoice>().toList().size }) {
        invoices.asFlow<InvoiceCsv>()
                .map { it.asInvoice() }
                .hotChunks()
                .flatMapConcat { invoiceRepository.insert(it).asFlow() }
                .map { it.id ?: "unknown" }
    }

    @FlowPreview
    @Transactional
    suspend fun uploadDbCached(invoices: FilePart): SubmissionRequest {
        val uuid: String = uuid()
        val invoiceFlow = invoices.asFlow<InvoiceCsv>().map { it.asInvoice(uuid) }
        val submissionRequest = SubmissionRequest(uuid, invoiceFlow.count())
        GlobalScope.launch {
            uploadRequestRepository.insert(submissionRequest)
                    .asFlow().collect()
            invoiceFlow.hotChunks()
                    .flatMapConcat { invoiceRepository.insert(it).asFlow() }
                    .map { it.id ?: "unknown" }
                    .onCompletion {
                        uploadRequestRepository.findById(uuid).asFlow()
                                .map{SubmissionRequest(it.id, it.count, true)}
                                .flatMapConcat{uploadRequestRepository.save(it).asFlow()}.collect()
                    }
                    .collect()
        }
        return submissionRequest
    }

    fun uploadProgress(uuid: String): Flow<SubmissionState> {
        val uploaded: Flow<Int> = invoiceRepository.countAllByUploadId(uuid).asFlow()
        val submissionRequest: Flow<SubmissionRequest> = uploadRequestRepository.findById(uuid).asFlow()
        return uploaded.zip(submissionRequest) { uploadedInt, uploadReq ->
            SubmissionState(uploadedInt, uploadReq.count, uploadReq.complete)
        }
    }

    fun InvoiceCsv.asInvoice(requestId: String? = null): Invoice =
            Invoice(
                    invoiceNo = this.invoiceNo,
                    stockCode = this.stockCode,
                    description = this.description,
                    quantity = this.quantity,
                    invoiceDate = this.invoiceDate,
                    unitPrice = this.unitPrice,
                    customerId = this.customerId,
                    country = this.country,
                    uploadId = requestId
            )
}
