package ru.alxr.moviedatabasedemo.pagination

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.alxr.moviedatabasedemo.utils.SingleDisposable

/**
 * This approach use is to handle really big list to avoid OOM Error.
 * Let's say we have collection of 1M records...
 *
 * As you can see from code below, we have in memory maximum three pages at the same time.
 *
 * This implementation is not perfect: we can catch exception if we will scroll up too fast.
 * To fix it we should play with page size or invent more sophisticated method.
 */
class DataProvider<Item>(
    initialPage: InitialPage<Item>,
    private val repository: IPageRepository<Item>,
    private val pageStorage: IPageStorage<Item>,
    private val callback: (Boolean) -> Unit
) : IDataProvider<Item> {

    private var currentPage: IPage<Item> = initialPage.copy()
    private var previousPage: IPage<Item>? = null
    private var nextPage: IPage<Item>? = null

    private val initialPageSize = initialPage.getSize()
    private var size: Int = initialPage.getSize()

    private val swapLock: Any = Any()

    private var lastRequestedItemIndex: Int = -1

    private var lockNextPage: Any? = null
    private var lockPreviousPage: Any? = null
    private var nextPageDisposable: Disposable? = null
    private var previousPageDisposable: Disposable? = null

    override fun getCount(): Int {
        return size
    }

    override fun getPageSize(): Int {
        return initialPageSize
    }

    override fun getItem(position: Int): Item {
        synchronized(swapLock) {
            lastRequestedItemIndex = position
            if (size == 0) throw RuntimeException("No data")
            val previousPageRange: IntRange? = getNullableRange(previousPage)
            val currentPageRange: IntRange = getRange(currentPage)
            val nextPageRange: IntRange? = getNullableRange(nextPage)
            checkRange(
                position = position,
                previousPageRange = previousPageRange,
                currentPageRange = currentPageRange,
                nextPageRange = nextPageRange
            )
            if (position.inRange(currentPageRange)) return currentPage.getItem(position)
            if (position.inRange(previousPageRange)) return previousPage?.getItem(position) ?: throw RuntimeException()
            if (position.inRange(nextPageRange)) return nextPage?.getItem(position) ?: throw RuntimeException()
            return currentPage.getItem(position)
        }
    }

    private fun getNullableRange(page: IPage<Item>?): IntRange? {
        if (page == null) return null
        return page.getOffset()..(page.getOffset() + page.getSize())
    }

    private fun getRange(page: IPage<Item>): IntRange {
        return page.getOffset()..(page.getOffset() + page.getSize())
    }

    private fun Int.inRange(range: IntRange?): Boolean {
        if (range == null) return false
        if (range.endInclusive == this) return false
        return this in range
    }

    private fun checkRange(
        position: Int,
        previousPageRange: IntRange?,
        currentPageRange: IntRange,
        nextPageRange: IntRange?
    ) {
        if (position.inRange(previousPageRange)) {
            loadPreviousPage(previousPageRange!!)
        } else {
            if (position.inRange(currentPageRange)) {
                loadNextPage(currentPageRange, nextPageRange)
            } else {
                if (position.inRange(nextPageRange)) {
                    loadNextPage(nextPageRange!!, null)
                } else {
                    throw RuntimeException("Unknown range $position")
                }
            }
        }
    }

    private fun loadNextPage(currentPageRange: IntRange, nextPageRange: IntRange?) {
        if (nextPageRange != null) return
        if (lockNextPage != null) return
        val currentPageSize = currentPageRange.endInclusive - currentPageRange.start
        if (currentPageSize < initialPageSize) return
        callback.invoke(true)
        lockNextPage = Any()
        nextPageDisposable = repository
            .loadNextPage(currentPageRange.endInclusive)
            .flatMap { pageStorage.storePage(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(SingleDisposable(this::onNextPageLoaded))
    }

    private fun loadPreviousPage(currentPageRange: IntRange) {
        if (lockPreviousPage != null) return
        lockPreviousPage = Any()
        previousPageDisposable = pageStorage
            .getPage(currentPageRange.start - initialPageSize)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                SingleDisposable(
                    success = {
                        if (it.isEmpty()) {
                            lockPreviousPage = null
                        } else {
                            onPreviousPageLoaded(it[0])
                        }
                    },
                    fail = {
                        lockPreviousPage = null
                    })
            )
    }

    private fun onPreviousPageLoaded(previousPageCandidate: IPage<Item>) {
        synchronized(swapLock) {
            try {
                val last = lastRequestedItemIndex
                if (last.inRange(getNullableRange(previousPage)) && isNext(previousPageCandidate, previousPage!!)) {
                    nextPage = currentPage
                    currentPage = previousPage!!
                    previousPage = previousPageCandidate
                }
            } finally {
                lockPreviousPage = null
            }
        }
    }

    private fun isNext(previousPageCandidate: IPage<Item>, page: IPage<Item>): Boolean {
        val candidateRange = getRange(previousPageCandidate)
        val range = getRange(page)
        return candidateRange.endInclusive == range.start
    }

    private fun onNextPageLoaded(nextPageCandidate: IPage<Item>) {
        synchronized(swapLock) {
            try {
                val last = lastRequestedItemIndex
                if (last.inRange(getNullableRange(previousPage))) {
                    return
                }

                if (last.inRange(getNullableRange(currentPage))) {
                    if (nextPage == null) {
                        nextPage = nextPageCandidate
                    }
                } else {
                    if (last.inRange(getNullableRange(nextPage))) {
                        previousPage = currentPage
                        currentPage = nextPage!!
                        nextPage = nextPageCandidate
                    }
                }
            } finally {
                nextPage?.apply {
                    size = getOffset() + getSize()
                }
                lockNextPage = null
                callback.invoke(false)
            }
        }
    }

}