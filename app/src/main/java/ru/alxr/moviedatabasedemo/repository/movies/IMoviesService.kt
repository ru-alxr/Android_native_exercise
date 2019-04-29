package ru.alxr.moviedatabasedemo.repository.movies

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface IMoviesService {

    @GET("movie/popular")
    fun getPopular(
        @Query("api_key") key: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "US"
    ): Single<MoviesResponse>

    @GET("movie/top_rated")
    fun getTopRated(
        @Query("api_key") key: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "US"
    ): Single<MoviesResponse>

}