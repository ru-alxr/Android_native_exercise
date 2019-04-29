package ru.alxr.moviedatabasedemo.pagination

import androidx.recyclerview.widget.RecyclerView

abstract class PagingAdapter<ViewHolder: RecyclerView.ViewHolder, Item>(
    private val provider: IDataProvider<Item>)

    : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return provider.getCount()
    }

    fun getItem(position:Int):Item{
        return provider.getItem(position)
    }

}