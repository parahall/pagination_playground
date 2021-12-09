package com.example.playground

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

object Repo {
    private val BlaBlaFlow: Flow<List<MyData>>
    val firstList = mutableListOf<CollectionDataMutable>()
    var secondList: List<CollectionDataMutable>? = null
    var mappedList: Map<Int, CollectionDataMutable>? = null

    init {
        collectionCheck()
        BlaBlaFlow = flow {
            val data = mutableListOf<MyData>()
            repeat(1000) {
                data.add(MyData("data_${it}"))
            }
            emit(data)
            delay(100000)
        }
    }

    fun getData(observer: (List<MyData>) -> Unit) {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            BlaBlaFlow.onCompletion { Log.d("REPO!!!", "getData: Completed") }
                .collect {
                    observer(it)
                }
        }
    }

    fun collectionCheck() {
        repeat(10) {
            val element = CollectionDataMutable("first_$it")
            firstList.add(element)
            element.name += "_changed"
        }

        secondList = firstList.filter { !it.name.contains("first").not() }
        mappedList = firstList.associateBy { it.hashCode() }
    }

}

data class MyData(val name: String)
data class CollectionData(val name: String)
class CollectionDataMutable(var name: String)