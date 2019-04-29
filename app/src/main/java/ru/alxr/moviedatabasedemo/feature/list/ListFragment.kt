package ru.alxr.moviedatabasedemo.feature.list

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import ru.alxr.moviedatabasedemo.R
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.pagination.IDataProvider
import ru.alxr.moviedatabasedemo.utils.Event

private const val LIST_STATE = "LIST_STATE"

class ListFragment : Fragment(), Observer<ListModel>, PageMovieAdapter.Callback {

    private val mViewModel: ListViewModel by viewModel()
    private val mInflater: LayoutInflater by inject()

    private var mListState: Parcelable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.getSavedState()?.apply {
            mListState = getParcelable(LIST_STATE)
        }
        mViewModel.getModel().observe(this, this)
    }

    override fun onChanged(model: ListModel?) {
        if (model == null || recycler_view == null) return
        handleInitialPageLoad(model.dataProvider)
        handleDataSetIncrement(model.onNextPageLoaded)
        handleLoadingNextPageProgress(model.loadNextPage)
    }

    override fun onPause() {
        super.onPause()
        recycler_view?.apply {
            val state = layoutManager?.onSaveInstanceState() ?: return
            val savedState = Bundle().apply { putParcelable(LIST_STATE, state) }
            mViewModel.setSavedState(savedState)
        }
    }

    private fun handleInitialPageLoad(dataProvider: IDataProvider<MovieEntity>?) {
        if (dataProvider == null || recycler_view == null) return
        recycler_view.apply {
            if (adapter == null) {
                adapter = PageMovieAdapter(
                    inflater = mInflater,
                    provider = dataProvider,
                    callback = this@ListFragment
                )
                adapter?.notifyDataSetChanged()
            }
            if (layoutManager == null) {
                val manager = LinearLayoutManager(activity)
                layoutManager = manager
                mListState?.apply { manager.onRestoreInstanceState(this) }
            }
        }
    }

    override fun onClicked(entity: MovieEntity) {
        mViewModel.onSelected(entity)
    }

    private fun handleDataSetIncrement(event: Event<Boolean>) {
        val eventContent = event.getContent() ?: return
        val adapter: PageMovieAdapter = recycler_view?.adapter as PageMovieAdapter? ?: return
        if (eventContent) adapter.onLoadNextPageComplete()
    }

    private fun handleLoadingNextPageProgress(value: Boolean) {
        val adapter: PageMovieAdapter = recycler_view?.adapter as PageMovieAdapter? ?: return
        adapter.onLoadNextPageProgress(value)
    }

}