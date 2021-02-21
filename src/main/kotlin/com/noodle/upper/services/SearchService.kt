package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper.hotChunks
import com.noodle.upper.utility.FlowHelper.track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component

@Component
class SearchService(@Autowired val reactiveInvoiceRepository: ReactiveInvoiceRepository) {
    //    fun search(strings: Array<String>): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
    suspend fun search(strings: Array<String>): Flow<Tracked<Invoice>> {
        val criteria: TextCriteria = TextCriteria.forDefaultLanguage().matchingAny(*strings)
        val count: Int = reactiveInvoiceRepository.countAllBy(criteria).asFlow().first()
//        track({ reactiveInvoiceRepository.findAllBy(criteria).asFlow() }, count)
        return track({ reactiveInvoiceRepository.findAllBy(criteria).asFlow() }, count)
//                .onEach { emit(ServerSentEvent.builder<Tracked<Invoice>>(it).build()) }
    }

    //    fun search(string: String): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
    suspend fun search(string: String): Flow<Tracked<Invoice>> {
        val stringList: List<String> = string.splitToSequence(" ").toList()
        val stringArray: Array<String> = stringList.toTypedArray()
//        search(stringArray).onEach { emit(it) }.collect()
        return search(stringArray)
    }

//    fun searchChunked(string: String): Flow<ServerSentEvent<Tracked<List<Invoice>>>> {
    suspend fun searchChunked(string: String): Flow<ServerSentEvent<Tracked<List<Invoice>>>> {
        val chunkSize = 1000
        return search(string)
                .hotChunks(chunkSize)
                .map {
                    Tracked(
                            it.maxOf { tracked -> tracked.fetched },
                            it[0].total,
                            it.fold(listOf<Invoice>()) {acc,cur -> acc+(cur.entity?:Invoice())}
                    )
//                    Tracked(
//                            it.maxOf { sse -> sse.data()?.fetched ?: 0 },
//                            it[0].data()?.total ?: 0,
//                            it.fold(listOf<Invoice>()) { acc, cur ->
//                                acc + mutableListOf(cur.data()?.entity ?: Invoice())
//                            })
                }
                .map { ServerSentEvent.builder(it).build() }

    }
}
