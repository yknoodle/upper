package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component

@Component
class ListService(@Autowired val reactiveInvoiceRepository: ReactiveInvoiceRepository) {
    fun list(pageable: Pageable): Flow<ServerSentEvent<Invoice>> = flow {
        val count: Int = pageable.pageSize
        emit(ServerSentEvent.builder<Invoice>().comment("fetching $count invoices").build())
        FlowHelper.progress({reactiveInvoiceRepository.findAllBy(pageable).asFlow()}, count, true).onEach{emit(it)}.collect()
    }
    fun list(page: Int, size: Int): Flow<ServerSentEvent<Invoice>> = flow {
        val pageRequest: Pageable = PageRequest.of(page, size)
        list(pageRequest).onEach{emit(it)}.collect()
    }
}
