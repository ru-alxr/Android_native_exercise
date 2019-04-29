package ru.alxr.moviedatabasedemo.repository.movies

import io.reactivex.Single
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.pagination.IPage
import ru.alxr.moviedatabasedemo.pagination.InitialPage

interface IMovieListRepository {

    fun getInitialPage(): Single<InitialPage<MovieEntity>>

    fun getPage(offset: Int): Single<IPage<MovieEntity>>

}