package ru.alxr.moviedatabasedemo.main

import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import org.koin.android.viewmodel.ext.android.viewModel
import ru.alxr.moviedatabasedemo.R
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity
import ru.alxr.moviedatabasedemo.navigation.NAVIGATE_BACK_TO_LIST
import ru.alxr.moviedatabasedemo.navigation.NAVIGATE_DETAILS

const val PAYLOAD_1 = "PAYLOAD_1"

class MainActivity : AppCompatActivity(), Observer<MainModel> {

    private val mViewModel: MainViewModel by viewModel()

    private lateinit var mNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNavController = Navigation
            .findNavController(
                this@MainActivity,
                R.id.nav_host_fragment
            )
        mNavController.setGraph(R.navigation.nav_graph_phone)
        mViewModel.getModel().observe(this, this)
    }

    override fun onChanged(model: MainModel?) {
        if (model == null) return
        if (model.isTablet) {
            throw RuntimeException("Not supported yet")
        }
        when (model.feature) {
            NAVIGATE_BACK_TO_LIST -> mNavController.popBackStack()
            NAVIGATE_DETAILS -> {
                model.featureArgs?.apply {
                    val entity = getContent() ?:return
                    val bundle = Bundle()
                    bundle.putParcelable(PAYLOAD_1, entity.toParcel())
                    mNavController.navigate(R.id.action_open_details, bundle)
                }
            }
            else -> return
        }
        mViewModel.onNavigationHandled()
    }

}

private fun MovieEntity.toParcel():Parcelable{

    return MovieParcel(
        voteAverage = voteAverage,
        backdropPath = backdropPath,
        originalTitle = originalTitle,
        overview = overview,
        popularity = popularity,
        posterPath = posterPath,
        releaseDate = releaseDate
    )

}