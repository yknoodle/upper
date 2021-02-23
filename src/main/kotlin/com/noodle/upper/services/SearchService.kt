package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.models.mergeTracked
import com.noodle.upper.models.track
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.hotChunks
import com.noodle.upper.utility.unique
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.stereotype.Component

@Component
class SearchService(@Autowired val invoiceRepository: ReactiveInvoiceRepository) {
    @ExperimentalCoroutinesApi
    @FlowPreview
    suspend fun searchPhrase(string: String): Flow<Tracked<List<Invoice>>> {
        val chunkSize = 1000
        val phrases: List<String> = listOf(string)
        val sort: Sort = Sort.by("score")
        val phraseCriteria: Flow<TextCriteria> = phrases.asFlow()
                .map { TextCriteria.forDefaultLanguage().matchingPhrase(it) }
        val phraseQuery: Flow<Invoice> = phraseCriteria
                .flatMapConcat { invoiceRepository.findAllBy(it, sort).asFlow() }
        val phraseQueryCount: Long = phraseCriteria
                .flatMapConcat { invoiceRepository.countAllBy(it).asFlow() }
                .runningReduce { acc, cur -> acc + cur }
                .catch { println(it) }
                .toList().lastOrNull() ?: 0
        return track({ phraseQuery.unique { it.id } }, phraseQueryCount)
                .hotChunks(chunkSize)
                .mergeTracked()
    }
}
