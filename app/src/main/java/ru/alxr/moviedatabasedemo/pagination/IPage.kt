package ru.alxr.moviedatabasedemo.pagination

interface IPage<Item> {

    fun getOffset():Int

    fun getSize():Int

    fun getItem(absolutePosition:Int):Item

}