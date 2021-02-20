package com.noodle.upper.services

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.noodle.upper.services.LoaderCache.expects
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

object Loader{
    fun completion(key: String): Map<String, Int> =
            mapOf("loaded" to LoaderCache.cacheOf(key).size, "of" to LoaderCache.expectedOf(key))

    fun <T> Flow<T>.collectTo(key: String): Flow<T> = this
            .onEach{ LoaderCache.cacheOf(key) +=mutableListOf(it)}
            .onEach{println(LoaderCache.cacheOf(key).size)}
    fun <T> load(expectedGetter: suspend () -> Int, source: () -> Flow<T>): String {
        val requestId = uuid()
        GlobalScope.launch {
            requestId expects expectedGetter()
            source().collectTo(requestId).collect()
        }
        return requestId
    }
}
fun uuid() = UUID.randomUUID().toString()
private object LoaderCache {
    const val maximumSize: Long = 3 //make configurable
    val _cache: Cache<String, MutableList<Any>> = CacheBuilder.newBuilder()
            .maximumSize(maximumSize).build()
    fun cacheOf(key: String): MutableList<Any> =
            _cache.get(key){mutableListOf()}
    fun expectedOf(key: String): Int =
            _expected.getOrPut(key){0}
    infix fun String.expects(expected: Int): Int? = _expected.put(this, expected)
//    private val _cache = mutableMapOf<String, MutableList<Any>>()
    private val _expected = mutableMapOf<String, Int>()
}