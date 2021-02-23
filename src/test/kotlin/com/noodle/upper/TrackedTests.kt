package com.noodle.upper

import com.noodle.upper.models.Tracked
import com.noodle.upper.models.completion
import com.noodle.upper.models.mergeTracked
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class TrackedTests {
    @Test
    fun calculateCompletion(){
        assert(
                Tracked(1,10,"something")
                        .completion()==0.1F)
    }
    @Test
    fun canMergeTracked() = runBlocking {
        val flowOfListOfTracked: Flow<List<Tracked<Int>>> = flowOf(listOf(
                Tracked(1, 10,1),
                Tracked(2, 10, 2)
        ))
        val result: Tracked<List<Int>> = flowOfListOfTracked.mergeTracked().single()
        assert(result.entity?.size == 2)
    }
}