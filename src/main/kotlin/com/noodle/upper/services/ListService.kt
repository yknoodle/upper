package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper.hotChunks
import com.noodle.upper.utility.FlowHelper.track
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component

@Component
class ListService(@Autowired val reactiveInvoiceRepository: ReactiveInvoiceRepository) {
    fun list(pageable: Pageable): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
        val count: Long = pageable.pageSize.toLong()
        track({reactiveInvoiceRepository.findAllBy(pageable).asFlow()},count)
                .onEach{emit(ServerSentEvent.builder<Tracked<Invoice>>(it).build())}.collect()
    }
    fun list(page: Int, size: Int): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
        val pageRequest: Pageable = PageRequest.of(page, size)
        list(pageRequest).onEach{emit(it)}.collect()
    }
    fun listChunked(page: Int, size: Int): Flow<ServerSentEvent<Tracked<List<Invoice>>>> {
        val chunkSize = 30
        return list(page, size)
                .hotChunks(chunkSize)
                .map{Tracked(
                        it.maxOf{sse -> sse.data()?.fetched?:0 },
                        it[0].data()?.total?:0,
                        it.fold(listOf<Invoice>()){
                            acc, cur -> acc+mutableListOf(cur.data()?.entity?:Invoice())})}
                .map{ServerSentEvent.builder(it).build()}
    }
}
