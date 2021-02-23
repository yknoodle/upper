package com.noodle.upper

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.SubmissionRequest
import com.noodle.upper.models.SubmissionState
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.repositories.UploadRequestRepository
import com.noodle.upper.services.UploadService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UploadServiceTests {
    @Test
    fun canGetUploadProgress(
            @Autowired invoiceRepository: ReactiveInvoiceRepository,
            @Autowired uploadRequestRepository: UploadRequestRepository,
            @Autowired uploadService: UploadService
    ) = runBlocking {
        val uploadId = "uploadId1"
        invoiceRepository.insert(Invoice(uploadId = uploadId)).block()
        uploadRequestRepository.insert(SubmissionRequest(id = uploadId,10)).block()
        val submissionState: SubmissionState = uploadService.uploadProgress(uploadId).first()
        assert(submissionState.total==10)
    }
    @Test
    fun uploadNotFound(
            @Autowired invoiceRepository: ReactiveInvoiceRepository,
            @Autowired uploadRequestRepository: UploadRequestRepository,
            @Autowired uploadService: UploadService
    ) = runBlocking {
        val uploadId = "uploadId1"
        val submissionState: SubmissionState? = uploadService.uploadProgress(uploadId).firstOrNull()
        assert(submissionState==null)
    }
}