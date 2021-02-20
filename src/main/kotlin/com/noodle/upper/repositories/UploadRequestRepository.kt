package com.noodle.upper.repositories

import com.noodle.upper.models.UploadRequest
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface UploadRequestRepository: ReactiveMongoRepository<UploadRequest, String>{
}