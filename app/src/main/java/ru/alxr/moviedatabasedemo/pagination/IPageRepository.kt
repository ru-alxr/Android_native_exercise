package ru.alxr.moviedatabasedemo.pagination

import io.reactivex.Single

interface IPageRepository<Item> {

    fun loadNextPage(offset: Int): Single<IPage<Item>>

}