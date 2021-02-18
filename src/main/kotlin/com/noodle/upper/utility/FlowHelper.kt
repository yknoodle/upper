package com.noodle.upper.utility

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import org.springframework.http.codec.ServerSentEvent

object FlowHelper {
    fun <T> progress(flowSupplier: ()-> Flow<T>, expected: Int, data: Boolean=false) = flow {
        var retrieved = 0F
        val invoiceFlow = flowSupplier()
                .onEach{
                    val prevCompletion = ((retrieved)/expected * 100).toInt()
                    val completion = ((retrieved+1)/expected * 100).toInt()
                    if (completion!=prevCompletion) emit(ServerSentEvent.builder<T>().comment("$completion%").id("progress").build())
                    if (data) emit(ServerSentEvent.builder<T>(it).build())
                    retrieved++
                }
        val invoices = invoiceFlow.toList()
        emit(ServerSentEvent.builder<T>().comment("delivered ${invoices.size} objects").build())
    }
}