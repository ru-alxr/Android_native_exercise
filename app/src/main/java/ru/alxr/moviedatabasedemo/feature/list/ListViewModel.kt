package ru.alxr.moviedatabasedemo.feature.list

import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.navigation.IFeatureNavigation
import ru.alxr.moviedatabasedemo.navigation.NAVIGATE_DETAILS
import ru.alxr.moviedatabasedemo.pagination.*
import ru.alxr.moviedatabasedemo.repository.movies.IMovieListRepository
import ru.alxr.moviedatabasedemo.utils.Event
import ru.alxr.moviedatabasedemo.utils.ILogger
import ru.alxr.moviedatabasedemo.utils.SingleDisposable
import java.util.*

class ListViewModel(
    private val nav: IFeatureNavigation,
    private val logger: ILogger,
    private val repo: IMovieListRepository
) : ViewModel(), IPageRepository<MovieEntity>, IPageStorage<MovieEntity> {

    override fun loadNextPage(offset: Int): Single<IPage<MovieEntity>> {
        if (getLoadedOffset() > offset) return getPage(offset)
            .flatMap {
                if (it.isEmpty()) Single.just(EmptyPage(offset))
                else Single.just(it[0])
            }
        return repo.getPage(offset)
    }

    override fun getPage(offset: Int): Single<List<IPage<MovieEntity>>> {
        return Single.fromCallable {
            if (Thread.currentThread() == Looper.getMainLooper().thread) throw RuntimeException("Main thread operation")
            Thread.sleep(100L)
            // TODO: get page from local database
            val page: IPage<MovieEntity>? = pages[offset]
            logger.with(this).add("On page found for offset $offset : $page").log()
            if (page == null) Collections.emptyList() else Collections.singletonList(page)
        }
    }

    override fun <Page : IPage<MovieEntity>> storePage(page: Page): Single<Page> {
        return Single
            .fromCallable {
                if (Thread.currentThread() == Looper.getMainLooper().thread) throw RuntimeException("Main thread operation")
                Thread.sleep(100L)
                pages[page.getOffset()] = page
                page
            }
    }

    private val pages: MutableMap<Int, IPage<MovieEntity>> = HashMap()
    //    private var mLiveModel: MutableLiveData<ListModel> = MutableLiveData()
    private var mLiveModel: MutableLiveData<ListModel>
    private var mDisposable: Disposable? = null

    init {
        mLiveModel = MutableLiveData()
        loadInitialPage()
        logger.with(this@ListViewModel).add("Constructor").log()
    }

    private fun getLoadedOffset(): Int {
        val model = mLiveModel.value ?: return 0
        val provider = model.dataProvider ?: return 0
        val size = provider.getCount()
        val pageSize = provider.getPageSize()
        return size - pageSize
    }

    private fun loadInitialPage() {
        mLiveModel.value = ListModel(loadingInitialPage = true)
        mDisposable = repo
            .getInitialPage()
            .flatMap { storePage(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(SingleDisposable(
                success = {
                    val model = mLiveModel.value ?: return@SingleDisposable
                    val initialPage: InitialPage<MovieEntity> = it
                    val repository: IPageRepository<MovieEntity> = this@ListViewModel
                    val pageStorage: IPageStorage<MovieEntity> = this@ListViewModel
                    val updated = model.copy(
                        dataProvider = DataProvider(
                            initialPage = initialPage,
                            repository = repository,
                            pageStorage = pageStorage,
                            callback = { increment -> onLoadNextPageProgressChange(increment) }
                        ),
                        loadingInitialPage = false
                    )
                    mLiveModel.value = updated
                },
                fail = {
                    logger.with(this@ListViewModel).e(it)
                    val model = mLiveModel.value ?: return@SingleDisposable
                    val updated = model.copy(
                        loadingInitialPage = false
                    )
                    mLiveModel.value = updated
                }
            ))
    }

    fun getModel(): LiveData<ListModel> {
        val model = mLiveModel.value ?: throw RuntimeException()
        mLiveModel = MutableLiveData()
        mLiveModel.value = model
        return mLiveModel
    }

    fun onSelected(entity: MovieEntity) {
        nav.navigateFeature(NAVIGATE_DETAILS, Event(entity))
    }

    override fun onCleared() {
        mDisposable?.dispose()
        super.onCleared()
    }

    private fun onLoadNextPageProgressChange(progress: Boolean) {
        val model = mLiveModel.value ?: return
        val dataProvider = model.dataProvider ?: return
        if (!progress) {
            mLiveModel.value = model.copy(
                dataProvider = dataProvider,
                loadNextPage = progress,
                onNextPageLoaded = Event(true)
            )
        } else {
            mLiveModel.value = model.copy(dataProvider = dataProvider, loadNextPage = progress)
        }
    }

    fun getSavedState(): Bundle? {
        return savedInstanceState
    }

    fun setSavedState(bundle: Bundle?) {
        savedInstanceState = bundle
    }

    private var savedInstanceState: Bundle? = null

}

data class ListModel(
    val isTablet: Boolean = false,
    val dataProvider: IDataProvider<MovieEntity>? = null,
    val loadNextPage: Boolean = false,
    val loadingInitialPage: Boolean = false,
    val loadingNextPage: Boolean = false,
    val onNextPageLoaded: Event<Boolean> = Event(false)
)