package com.noodle.upper

import com.noodle.upper.utility.hotChunks
import com.noodle.upper.utility.unique
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * @author damienho
 * does not test the non-blocking nature of these functions
 */
class FlowHelperTests {
    @Test
    fun canTest() {
    }

    @Test
    fun hotChunksCreatesChunks() = runBlocking {
        val chunkSize = 3
        val testString = "a-long-string"
        val chunks: List<List<Char>> = testString.asSequence().asFlow()
                .hotChunks(chunkSize)
                .onEach { println(it) }
                .toList()
        println(chunks)
        assert(chunks.isNotEmpty())
        assert(chunks[0].size == chunkSize)
    }
    @Test
    fun hotChunksEmitsAll() = runBlocking {
        val chunks = 3
        val testString = "a-long-string"
        val chunked: List<List<Char>> = testString.asSequence().asFlow()
                .hotChunks(chunks)
                .onEach { println(it) }
                .toList()
        assert(testString.length == chunked.flatMapIndexed { _, char -> char }.size)
    }
    @Test
    fun uniqueGivesUnique() = runBlocking {
        val testString = "aabbcc"
        val unique: List<Char> = testString.asSequence().asFlow()
                .unique{it}
                .onEach{println(it)}
                .toList()
        assert(unique.size == testString.asSequence().asFlow().distinctUntilChanged().count())
    }
}