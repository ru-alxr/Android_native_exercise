package ru.alxr.moviedatabasedemo.repository.movies

import io.reactivex.Single
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.pagination.IPage
import ru.alxr.moviedatabasedemo.pagination.InitialPage
import ru.alxr.moviedatabasedemo.pagination.MoviePage
import ru.alxr.moviedatabasedemo.repository.credential.ICredentialRepository

class MovieListRepository(
    private val service: IMoviesService,
    private val credentialRepository: ICredentialRepository
) : IMovieListRepository {

    @Volatile
    private var mInitialPageSize: Int = -1

    override fun getInitialPage(): Single<InitialPage<MovieEntity>> {
        return credentialRepository
            .getMovieDatabaseCredential()
            .flatMap { service.getPopular(it) }
            .flatMap {
                mInitialPageSize = it.results.size
                Single.just(InitialPage(mapRemote(it.results)))
            }
    }

    override fun getPage(offset: Int): Single<IPage<MovieEntity>> {
        return credentialRepository
            .getMovieDatabaseCredential()
            .flatMap {
                val page = offset / mInitialPageSize + 1
                service.getPopular(key = it, page = page)
            }
            .flatMap { Single.just(MoviePage(mapRemote(it.results), offset)) }
    }

    private fun mapRemote(list: List<RemoteMovie>): List<MovieEntity> {
        val entities: MutableList<MovieEntity> = ArrayList()
        for (remote in list) {
            val entity = MovieEntity(
                id = remote.id,
                voteAverage = remote.voteAverage,
                popularity = remote.popularity,
                originalTitle = remote.originalTitle ?: "",
                overview = remote.overview ?: "",
                posterPath = remote.posterPath ?: "",
                backdropPath = remote.backdropPath ?: "",
                releaseDate = remote.releaseDate ?: ""
            )
            entities.add(entity)
        }
        return entities
    }

}