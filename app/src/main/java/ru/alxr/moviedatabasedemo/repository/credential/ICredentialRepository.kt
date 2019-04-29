package ru.alxr.moviedatabasedemo.repository.credential

import io.reactivex.Single

interface ICredentialRepository {

    fun getMovieDatabaseCredential(): Single<String>

}