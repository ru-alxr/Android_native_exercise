package ru.alxr.moviedatabasedemo.navigation

import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.utils.Event

class FeatureNavigation: IFeatureNavigation {

    private var mHandler: INavigationHandler? = null

    override fun detach() {
        mHandler = null
    }

    override fun attach(handler: INavigationHandler) {
        mHandler = handler
    }

    override fun navigateFeature(target: String, args: Event<MovieEntity>) {
        mHandler?.onFeatureRequested(target, args)
    }
}