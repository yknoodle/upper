package com.noodle.upper.models

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Tracked<T>(val fetched: Int, val total: Long, val entity: T?) {
}

fun <T> Tracked<T>.completion(): Float = this.fetched.toFloat()/this.total
fun <T> Tracked<T>.base100(): Int = (completion()*100).toInt()
fun <T> Flow<List<Tracked<T>>>.mergeTracked(): Flow<Tracked<List<T>>> {
    return this.map {
        Tracked(
                it.maxOf { tracked -> tracked.fetched },
                it[0].total,
                it.fold(listOf<T>()) { acc, cur -> if (cur.entity != null) acc + (cur.entity) else acc }
        )
    }
}