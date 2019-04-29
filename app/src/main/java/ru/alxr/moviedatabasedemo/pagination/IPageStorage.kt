package ru.alxr.moviedatabasedemo.pagination

import io.reactivex.Single

interface IPageStorage<Item> {

    fun getPage(offset: Int): Single<List<IPage<Item>>>

    fun <Page : IPage<Item>> storePage(page: Page): Single<Page>

}