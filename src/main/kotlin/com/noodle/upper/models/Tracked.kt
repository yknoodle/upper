package com.noodle.upper.models

data class Tracked<T>(val fetched: Int, val total: Long, val entity: T?) {
}

fun <T> Tracked<T>.completion(): Float = this.fetched.toFloat()/this.total
fun <T> Tracked<T>.base100(): Int = (completion()*100).toInt()