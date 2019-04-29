package ru.alxr.moviedatabasedemo.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.navigation.IFeatureNavigation
import ru.alxr.moviedatabasedemo.navigation.INavigationHandler
import ru.alxr.moviedatabasedemo.utils.Event

class MainViewModel(model: MainModel, private val nav: IFeatureNavigation) : ViewModel(), INavigationHandler {

    private val mLiveModel: MutableLiveData<MainModel> = MutableLiveData()

    init {
        mLiveModel.value = model
        nav.attach(this)
    }

    override fun onCleared() {
        nav.detach()
        super.onCleared()
    }

    fun getModel(): LiveData<MainModel> {
        return mLiveModel
    }

    override fun onFeatureRequested(target: String, args: Event<MovieEntity>) {
        val model = mLiveModel.value ?: return
        mLiveModel.value = model.copy(feature = target, featureArgs = args)
    }

    fun onNavigationHandled() {
        val model = mLiveModel.value ?: return
        mLiveModel.value = model.copy(feature = "", featureArgs = null)
    }

}

data class MainModel(val isTablet: Boolean, val feature: String = "", val featureArgs: Event<MovieEntity>? = null)