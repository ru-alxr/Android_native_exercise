package ru.alxr.moviedatabasedemo.pagination

import ru.alxr.moviedatabasedemo.db.entity.MovieEntity

class MoviePage(private val list: List<MovieEntity>, private val offset: Int) : IPage<MovieEntity> {

    override fun getOffset(): Int {
        return offset
    }

    override fun getSize(): Int {
        return list.size
    }

    override fun getItem(absolutePosition: Int): MovieEntity {
        return list[absolutePosition - offset]
    }

}