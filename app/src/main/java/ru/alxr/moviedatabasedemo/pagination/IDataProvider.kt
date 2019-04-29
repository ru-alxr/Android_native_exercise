package ru.alxr.moviedatabasedemo.pagination

interface IDataProvider<Item> {

    fun getCount():Int

    fun getItem(position:Int):Item

    fun getPageSize():Int

}