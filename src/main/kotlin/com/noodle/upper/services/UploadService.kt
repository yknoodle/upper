package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.InvoiceCsv
import com.noodle.upper.models.SubmissionRequest
import com.noodle.upper.models.SubmissionState
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.repositories.UploadRequestRepository
import com.noodle.upper.utility.asFlow
import com.noodle.upper.utility.hotChunks
import com.noodle.upper.utility.uuid
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UploadService(
        @Autowired val invoiceRepository: ReactiveInvoiceRepository,
        @Autowired val uploadRequestRepository: UploadRequestRepository) {

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
