package ru.alxr.moviedatabasedemo.feature.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.alxr.moviedatabasedemo.R
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.pagination.IDataProvider
import ru.alxr.moviedatabasedemo.pagination.PagingAdapter

class PageMovieAdapter(
    private val inflater: LayoutInflater,
    private val callback: Callback,
    provider: IDataProvider<MovieEntity>
) :

    PagingAdapter<PageMovieAdapter.MovieViewHolder, MovieEntity>(provider) {

    interface Callback {
        fun onClicked(entity: MovieEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = inflater.inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MovieViewHolder(
        view: View,
        private val titleView: TextView? = view.findViewById(R.id.title)
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            callback.onClicked(getItem(adapterPosition))
        }

        fun bind(entity: MovieEntity?) {
            val title = entity?.originalTitle ?: ""
            titleView?.text = title
        }
    }

    fun onLoadNextPageComplete() {
        notifyDataSetChanged()
    }

    fun onLoadNextPageProgress(value: Boolean) {

        // todo: add/remove progress widget

    }

}