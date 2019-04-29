package ru.alxr.moviedatabasedemo.feature.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.alxr.moviedatabasedemo.R
import kotlinx.android.synthetic.main.fragment_details.*
import ru.alxr.moviedatabasedemo.main.MovieParcel
import ru.alxr.moviedatabasedemo.main.PAYLOAD_1

class DetailsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val parcel: MovieParcel = arguments?.getParcelable(PAYLOAD_1) ?:return
        label?.text = parcel.toString()
    }

}