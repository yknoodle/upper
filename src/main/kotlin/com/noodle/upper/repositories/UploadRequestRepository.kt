package com.noodle.upper.repositories

import com.noodle.upper.models.SubmissionRequest
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UploadRequestRepository: ReactiveMongoRepository<SubmissionRequest, String>{
}