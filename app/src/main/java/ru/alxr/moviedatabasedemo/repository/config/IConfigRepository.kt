package ru.alxr.moviedatabasedemo.repository.config

import io.reactivex.Single

interface IConfigRepository {

    fun getServiceCredentials(): Single<String>

}