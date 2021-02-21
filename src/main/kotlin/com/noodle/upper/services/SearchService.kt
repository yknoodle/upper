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
import org.springframework.stereotype.Component

@Component
class SearchService(@Autowired val invoiceRepository: ReactiveInvoiceRepository) {
    suspend fun search(strings: Array<String>): Flow<Tracked<Invoice>> {
        val criteria: TextCriteria = TextCriteria.forDefaultLanguage().matchingAny(*strings)
        val count: Int = invoiceRepository.countAllBy(criteria).asFlow().first()
        return track({ invoiceRepository.findAllBy(criteria).asFlow() }, count)
    }

    suspend fun searchWords(string: String): Flow<Tracked<List<Invoice>>> {
        val chunkSize = 1000
        val stringList: List<String> = string.splitToSequence(" ").toList()
        val stringArray: Array<String> = stringList.toTypedArray()
        return search(stringArray)
                .hotChunks(chunkSize)
                .mergeTracked()
    }
    suspend fun defaultSearch(string: String): Flow<Tracked<List<Invoice>>> {
        println(string)
        val chunkSize = 1000
        val stringList: List<String> = string.splitToSequence(" ").toList()
        val stringArray: Array<String> = stringList.toTypedArray()
        val criteria: TextCriteria = TextCriteria.forDefaultLanguage().matchingPhrase("\"$string\"").matchingAny(*stringArray)
        val count: Int = invoiceRepository.countAllBy(criteria).asFlow().first()
        return track({invoiceRepository.findAllBy(criteria).asFlow()}, count)
                .hotChunks(chunkSize)
                .mergeTracked()
    }
    suspend fun <T> Flow<List<Tracked<T>>>.mergeTracked(): Flow<Tracked<List<T>>> {
        return this.map{
            Tracked(
                    it.maxOf { tracked -> tracked.fetched },
                    it[0].total,
                    it.fold(listOf<T>()) {acc,cur -> if (cur.entity != null)acc+(cur.entity) else acc}
            )
        }
    }
}
