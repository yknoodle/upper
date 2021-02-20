package com.noodle.upper.utility

import com.noodle.upper.models.Tracked
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

object FlowHelper {
    fun <T> track(flowSupplier: ()-> Flow<T>, expected: Int): Flow<Tracked<T>>  {
        var retrieved = 0
        return flowSupplier()
                .map{
                    val currentRetrieved = retrieved + 1
                    retrieved = currentRetrieved
                    Tracked(currentRetrieved, expected, it)
                }
    }
    fun <T> Flow<T>.chunked(maxSize: Int=5000):Flow<List<T>> = flow {
        val list: List<T> = toList()
        list.chunked(maxSize).forEach {emit(it)}
    }
}