package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper.track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component

@Component
class ListService(@Autowired val reactiveInvoiceRepository: ReactiveInvoiceRepository) {
    fun list(pageable: Pageable): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
        val count: Int = pageable.pageSize
        track({reactiveInvoiceRepository.findAllBy(pageable).asFlow()},count)
                .onEach{emit(ServerSentEvent.builder<Tracked<Invoice>>(it).build())}.collect()
    }
    fun list(page: Int, size: Int): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
        val pageRequest: Pageable = PageRequest.of(page, size)
        list(pageRequest).onEach{emit(it)}.collect()
    }
}
