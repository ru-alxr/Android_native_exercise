package ru.alxr.moviedatabasedemo.navigation

import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.utils.Event

const val NAVIGATE_BACK_TO_LIST = "NAVIGATE_BACK_TO_LIST"
const val NAVIGATE_DETAILS = "NAVIGATE_DETAILS"

interface IFeatureNavigation {

    fun navigateFeature(target: String, args: Event<MovieEntity>)

    fun attach(handler: INavigationHandler)

    fun detach()

}

interface INavigationHandler {

    fun onFeatureRequested(target: String, args:Event<MovieEntity>)

}