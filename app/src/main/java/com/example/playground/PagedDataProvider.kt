package com.example.playground

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PagedDataProvider {

    private val offsetFlow: MutableSharedFlow<Int> = MutableSharedFlow(1)
    var total: Int = 0
    private var lastOffset = 0
    private var lastKnownVisibleItemPosition = 0

    private var upperStart = 0L
    private var bottomStart = 0L

    init {
        offsetFlow.tryEmit(0)
    }

    private fun onBottomThresholdReached() {
        GlobalScope.launch {
            Log.d("PagedDataProvider", "onBottomThresholdReached with previous offset: $lastOffset")
            if (lastOffset >= total) return@launch
            val offset = lastOffset + PAGE_SIZE

            Log.d("PagedDataProvider", "onBottomThresholdReached new offset: $offset")
            bottomStart = System.currentTimeMillis()
            upperStart = 0
            offsetFlow.emit(offset)
        }
    }

    private fun onUpperThresholdReached() {
        GlobalScope.launch {
            Log.d("PagedDataProvider", "onUpperThresholdReached with previous offset: $lastOffset")
            if (lastOffset == 0) return@launch
            val offset = lastOffset - PAGE_SIZE

            Log.d("PagedDataProvider", "onUpperThresholdReached new offset: $offset")
            bottomStart = 0
            upperStart = System.currentTimeMillis()
            offsetFlow.emit(offset)
        }
    }

    suspend fun observeData(): Flow<List<User>> {
        total = DBRepo.getTotalUsers()
        return offsetFlow.distinctUntilChanged().flatMapLatest { offset ->
            DBRepo.getUsers(offset , DB_LIMIT)
        }.onEach {
            val measure = if(upperStart == 0L) {
                System.currentTimeMillis() - bottomStart
            } else {
                System.currentTimeMillis() - upperStart
            }
            Log.d("PagedDataProvider", "measure $measure")
            lastOffset = offsetFlow.first()
        }
    }

    fun onScroll(firstVisibleItemPosition: Int) {
        if (firstVisibleItemPosition >= THRESHOLD_BOTTOM) {
            Log.d(
                "PagedDataProvider",
                "first visible item passed threshold position: $firstVisibleItemPosition, threshold: $THRESHOLD_BOTTOM"
            )
            onBottomThresholdReached()
        }

        if(firstVisibleItemPosition <= THRESHOLD_UPPER) {
            onUpperThresholdReached()
        }
    }

    companion object {
        const val PAGE_SIZE = 30
        const val DB_LIMIT = PAGE_SIZE * 4
        const val THRESHOLD_BOTTOM = PAGE_SIZE * 2
        const val THRESHOLD_UPPER = PAGE_SIZE
    }
}
