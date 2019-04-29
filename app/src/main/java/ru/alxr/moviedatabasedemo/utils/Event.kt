package ru.alxr.moviedatabasedemo.utils

class Event<T>(private val content: T) {

    private var consumed: Boolean = false

    fun getContent(): T? {
        if (consumed) return null
        consumed = true
        return content
    }

}