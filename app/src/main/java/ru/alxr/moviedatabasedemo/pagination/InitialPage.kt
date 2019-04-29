package ru.alxr.moviedatabasedemo.pagination

data class InitialPage<Item>(private val initialData: List<Item>) : IPage<Item>{

    override fun getOffset(): Int {
        return 0
    }

    override fun getSize(): Int {
        return initialData.size
    }

    override fun getItem(absolutePosition: Int): Item {
        return initialData[absolutePosition]
    }

}