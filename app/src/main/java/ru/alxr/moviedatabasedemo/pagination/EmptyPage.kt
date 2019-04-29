package ru.alxr.moviedatabasedemo.pagination

import java.lang.RuntimeException

class EmptyPage<Item>(private val offset:Int) : IPage<Item> {
    override fun getOffset(): Int {
        return offset
    }

    override fun getSize(): Int {
        return 0
    }

    override fun getItem(absolutePosition: Int): Item {
        throw RuntimeException("Empty page")
    }

}