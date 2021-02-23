package com.noodle.upper.utility

import com.noodle.upper.models.Tracked
import kotlinx.coroutines.flow.*

fun <T> track(flowSupplier: () -> Flow<T>, expected: Long): Flow<Tracked<T>> {
    var retrieved = 0
    return flowSupplier()
            .map {
                val currentRetrieved = retrieved + 1
                retrieved = currentRetrieved
                Tracked(currentRetrieved, expected, it)
            }
}

/**
 * @deprecated not tested, may cause unexpected behaviour
 */
fun <T> Flow<T>.chunks(maxSize: Int = 5000): Flow<List<T>> = flow {
    val list: List<T> = toList()
    list.chunked(maxSize).forEach { emit(it) }
}

/**
 * @author damienho
 * @param maxSize is the largest possible chunk
 * @return a list of chunks
 * chunks the incoming flow into Lists without blocking the flow
 */
fun <T> Flow<T>.hotChunks(maxSize: Int = 5000): Flow<List<T>> = flow {
    var list: MutableList<T> = mutableListOf()
    onEach {
        list.add(it)
        if (list.size == maxSize) {
            emit(list)
            list = mutableListOf()
        }
    }.onCompletion { if (list.isNotEmpty()) emit(list) } .collect()
}

/**
 * @author damienho
 * @param keySelector choose which property of T that you want to compare
 * @return a unique flow of objects based on the key selected
 * streams a unique list of objects to the subscriber without blocking the flow
 */
fun <T, K> Flow<T>.unique(keySelector: (T) -> K): Flow<T> = flow {
    val keySet: MutableSet<K> = mutableSetOf()
    onEach {
        if (keySelector(it) !in keySet) {
            keySet.add(keySelector(it))
            emit(it)
        }
    }.collect()
}