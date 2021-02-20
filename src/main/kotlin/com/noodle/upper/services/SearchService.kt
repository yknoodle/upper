package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper.track
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component

@Component
class SearchService(@Autowired val reactiveInvoiceRepository: ReactiveInvoiceRepository) {
    fun search(strings: Array<String>): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
        val stringJoined: String = strings.joinToString(", ")
        emit(ServerSentEvent.builder<Tracked<Invoice>>().comment("begin search using $stringJoined").build())
        val criteria: TextCriteria = TextCriteria.forDefaultLanguage().matchingAny(*strings)
        val count: Int = reactiveInvoiceRepository.countAllBy(criteria).asFlow().first()
        track({reactiveInvoiceRepository.findAllBy(criteria).asFlow()},count)
                .onEach{emit(ServerSentEvent.builder<Tracked<Invoice>>(it).build())}.collect()
    }
    fun search(string: String): Flow<ServerSentEvent<Tracked<Invoice>>> = flow {
        val stringList: List<String> = string.splitToSequence(" ").toList()
        val stringArray: Array<String> = stringList.toTypedArray()
        search(stringArray).onEach{emit(it)}.collect()
    }
}
