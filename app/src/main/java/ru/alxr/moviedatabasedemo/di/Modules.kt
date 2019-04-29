package ru.alxr.moviedatabasedemo.di

import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import ru.alxr.moviedatabasedemo.db.AppDatabase
import ru.alxr.moviedatabasedemo.db.entity.MovieDAO
import ru.alxr.moviedatabasedemo.feature.details.DetailsViewModel
import ru.alxr.moviedatabasedemo.feature.list.ListViewModel
import ru.alxr.moviedatabasedemo.main.MainModel
import ru.alxr.moviedatabasedemo.main.MainViewModel
import ru.alxr.moviedatabasedemo.repository.credential.CredentialRepository
import ru.alxr.moviedatabasedemo.repository.credential.ICredentialRepository
import ru.alxr.moviedatabasedemo.repository.movies.IMovieListRepository
import ru.alxr.moviedatabasedemo.repository.movies.IMoviesService
import ru.alxr.moviedatabasedemo.repository.movies.MovieListRepository

val MAIN_VIEW_MODULE = module {

    viewModel { MainViewModel(MainModel(isTablet = false), nav = get()) }

    viewModel {
        ListViewModel(
            nav = get(),
            repo = get(),
            logger = get()
        )
    }

    single { getMovieDao(db = get()) }

    single { CredentialRepository(config = get(), logger = get()) as ICredentialRepository }

    single { getMovieService(retrofit = get()) }

    single {
        MovieListRepository(
            service = get(),
            credentialRepository = get()
        ) as IMovieListRepository
    }

}

val DETAILS_VIEW_MODULE = module {

    viewModel { DetailsViewModel(nav = get()) }

}

private fun getMovieService(retrofit: Retrofit): IMoviesService {
    return retrofit.create(IMoviesService::class.java)
}

private fun getMovieDao(db: AppDatabase): MovieDAO {
    return db.getMovieDAO()
}