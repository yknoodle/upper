package com.noodle.upper

import com.noodle.upper.models.Tracked
import com.noodle.upper.models.completion
import com.noodle.upper.models.mergeTracked
import com.noodle.upper.models.track
import kotlinx.coroutines.flow.*
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
    @Test
    fun canTrackFlow() = runBlocking {
        val flowOfEntities: Flow<Int> = flowOf(1,2,3)
        val result: List<Tracked<Int>> = track({ flowOfEntities }, flowOfEntities.count().toLong()).toList()
        assert(result.size == flowOfEntities.count())
        assert(result[0].fetched==1)
        assert(result[1].fetched==2)
        assert(result[0].total==3L)
    }
}