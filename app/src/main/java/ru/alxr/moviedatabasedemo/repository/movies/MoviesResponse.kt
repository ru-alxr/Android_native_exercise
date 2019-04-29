package ru.alxr.moviedatabasedemo.repository.movies

import com.squareup.moshi.Json

data class MoviesResponse(
    @Json(name = "page") val page:Int,
    @Json(name = "total_results") val totalResults:Int,
    @Json(name = "total_pages") val totalPages:Int,
    @Json(name = "results") val results:List<RemoteMovie>
)