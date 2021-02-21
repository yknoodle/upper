package com.noodle.upper.utility

import org.apache.commons.lang3.StringUtils
import java.util.*

object Strings {
}
fun uuid() = UUID.randomUUID().toString()
fun words(string: String): List<String> =
        StringUtils.replaceEach(string,
                phrases(string).map { "\"$it\"" }.toTypedArray(),
                phrases(string).map { "" }.toTypedArray())
                .splitToSequence(" ")
                .filter { it.isNotEmpty() }.toList()

fun phrases(string: String): List<String> =
        try {
            StringUtils.substringsBetween(string, "\"", "\"").toList()
        } catch (ex: Exception) {
            emptyList()
        }
