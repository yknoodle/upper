package com.noodle.upper.services

import com.noodle.upper.models.Invoice
import com.noodle.upper.models.Tracked
import com.noodle.upper.repositories.ReactiveInvoiceRepository
import com.noodle.upper.utility.FlowHelper.hotChunks
import com.noodle.upper.utility.FlowHelper.track
import com.noodle.upper.utility.FlowHelper.unique
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import org.apache.commons.lang3.StringUtils.replaceEach
import org.apache.commons.lang3.StringUtils.substringsBetween
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono.just

@Component
class SearchService(@Autowired val invoiceRepository: ReactiveInvoiceRepository) {
    suspend fun search(strings: Array<String>): Flow<Tracked<Invoice>> {
        val criteria: TextCriteria = TextCriteria.forDefaultLanguage().matchingAny(*strings)
        val count: Long = invoiceRepository.countAllBy(criteria).asFlow().first()
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
    @FlowPreview
    @ExperimentalCoroutinesApi
    suspend fun defaultSearch(string: String): Flow<Tracked<List<Invoice>>> {
        val chunkSize = 1000
        val words: List<String> = words(string)
        val phrases: List<String> = phrases(string)
        println("words: $words, phrases: $phrases")
        val sort: Sort = Sort.by("score")
        val phraseCriteria: Flow<TextCriteria> = phrases.asFlow()
                .map { TextCriteria.forDefaultLanguage().matchingPhrase(it) }
        val phraseQuery: Flow<Invoice> = phraseCriteria
                .flatMapConcat { invoiceRepository.findAllBy(it, sort).asFlow() }
        val wordsCriteria: TextCriteria = TextCriteria.forDefaultLanguage().matchingAny(*words.toTypedArray())
        val wordsQuery: Flow<Invoice> = invoiceRepository.findAllBy(
                wordsCriteria, sort).asFlow()
                .catch{println(it)}
        val wordQueryCount: Long = invoiceRepository.countAllBy(wordsCriteria)
                .onErrorResume{ just(0)}.asFlow()
                .catch{println(it)}.first()


        val phraseQueryCount: Long = phraseCriteria
                .flatMapConcat { invoiceRepository.countAllBy(it).asFlow() }
                .runningReduce { acc, cur -> acc + cur }
                .catch { println(it) }
                .toList().lastOrNull() ?: 0
        return track({ merge(wordsQuery, phraseQuery).unique { it.id } }, wordQueryCount + phraseQueryCount)
                .hotChunks(chunkSize)
                .mergeTracked()
    }

    private fun words(string: String): List<String> =
            replaceEach(string,
                    phrases(string).map { "\"$it\"" }.toTypedArray(),
                    phrases(string).map { "" }.toTypedArray())
                    .splitToSequence(" ")
                    .filter { it.isNotEmpty() }.toList()

    private fun phrases(string: String): List<String> =
            try {
                substringsBetween(string, "\"", "\"").toList()
            } catch (ex: Exception) {
                emptyList()
            }

    suspend fun <T> Flow<List<Tracked<T>>>.mergeTracked(): Flow<Tracked<List<T>>> {
        return this.map {
            Tracked(
                    it.maxOf { tracked -> tracked.fetched },
                    it[0].total,
                    it.fold(listOf<T>()) { acc, cur -> if (cur.entity != null) acc + (cur.entity) else acc }
            )
        }
    }
}
