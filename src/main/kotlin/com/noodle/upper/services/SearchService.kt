package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper.progress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component

@Component
class SearchService(@Autowired val reactiveInvoiceRepository: ReactiveInvoiceRepository) {
    fun search(strings: Array<String>): Flow<ServerSentEvent<Invoice>> = flow {
        val stringJoined: String = strings.joinToString(", ")
        emit(ServerSentEvent.builder<Invoice>().comment("begin search using $stringJoined").build())
        val criteria: TextCriteria = TextCriteria.forDefaultLanguage().matchingAny(*strings)
        val count: Int = reactiveInvoiceRepository.countAllBy(criteria).asFlow().first()
        emit(ServerSentEvent.builder<Invoice>().comment("fetching $count invoices").build())
        progress({reactiveInvoiceRepository.findAllBy(criteria).asFlow()},count, true).onEach{emit(it)}.collect()
    }
    fun search(string: String): Flow<ServerSentEvent<Invoice>> = flow {
        val stringList: List<String> = string.splitToSequence(" ").toList()
        val stringArray: Array<String> = stringList.toTypedArray()
        search(stringArray).onEach{emit(it)}.collect()
    }
}
