package ru.alxr.moviedatabasedemo.utils

import io.reactivex.observers.DisposableSingleObserver

class SingleDisposable<T>(private val success: (T) -> Unit = {}, private val fail: (Throwable) -> Unit = {}) :

    DisposableSingleObserver<T>() {
    override fun onSuccess(t: T) {
        success.invoke(t)
    }

    override fun onError(e: Throwable) {
        fail.invoke(e)
    }

}