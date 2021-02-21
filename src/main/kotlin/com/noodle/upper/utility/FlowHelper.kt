package com.noodle.upper.utility

import com.noodle.upper.models.Tracked
import kotlinx.coroutines.flow.*

object FlowHelper {
    fun <T> track(flowSupplier: () -> Flow<T>, expected: Long): Flow<Tracked<T>> {
        var retrieved = 0
        return flowSupplier()
                .map {
                    val currentRetrieved = retrieved + 1
                    retrieved = currentRetrieved
                    Tracked(currentRetrieved, expected, it)
                }
    }

    fun <T> Flow<T>.chunks(maxSize: Int = 5000): Flow<List<T>> = flow {
        val list: List<T> = toList()
        list.chunked(maxSize).forEach { emit(it) }
    }

    fun <T> Flow<T>.hotChunks(maxSize: Int = 5000): Flow<List<T>> = flow {
        val list: MutableList<T> = mutableListOf()
        onEach {
            list += mutableListOf(it)
            if (list.size == maxSize) {
                emit(list)
                list.clear()
            }
        }
                .onCompletion { if (list.isNotEmpty()) emit(list) }
                .collect()
    }

    fun <T, K> Flow<T>.unique(keySelector: (T) -> K): Flow<T> = flow {
        val keySet: MutableSet<K> = mutableSetOf()
        onEach{
            if (keySelector(it) !in keySet) {
                keySet.add(keySelector(it))
                emit(it)
            }
        }.collect()
    }
}