package com.noodle.upper

import com.noodle.upper.utility.hotChunks
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

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
}