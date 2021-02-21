package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.models.mergeTracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper.hotChunks
import com.noodle.upper.utility.FlowHelper.track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ListService(@Autowired val reactiveInvoiceRepository: ReactiveInvoiceRepository) {
    fun list(pageable: Pageable): Flow<Tracked<Invoice>> = flow {
        val count: Long = pageable.pageSize.toLong()
        track({reactiveInvoiceRepository.findAllBy(pageable).asFlow()},count)
                .onEach{emit(it)}.collect()
    }
    fun list(page: Int, size: Int): Flow<Tracked<Invoice>> = flow {
        val pageRequest: Pageable = PageRequest.of(page, size)
        list(pageRequest).onEach{emit(it)}.collect()
    }
    fun listChunked(page: Int, size: Int): Flow<Tracked<List<Invoice>>> {
        val chunkSize = 30 // make configurable
        return list(page, size)
                .hotChunks(chunkSize)
                .mergeTracked()
    }
}
