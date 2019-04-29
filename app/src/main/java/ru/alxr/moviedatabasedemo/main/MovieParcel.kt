package ru.alxr.moviedatabasedemo.main

import android.os.Parcel
import android.os.Parcelable

class MovieParcel(
    val voteAverage: Double,
    val popularity: Double,
    val backdropPath: String?,
    val posterPath: String?,
    val originalTitle: String?,
    val releaseDate: String?,
    val overview: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(voteAverage)
        parcel.writeDouble(popularity)
        parcel.writeString(backdropPath)
        parcel.writeString(posterPath)
        parcel.writeString(originalTitle)
        parcel.writeString(releaseDate)
        parcel.writeString(overview)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return originalTitle + "\n" + overview + "\n" + releaseDate + "\n" + voteAverage + "\n" + popularity
    }

    companion object CREATOR : Parcelable.Creator<MovieParcel> {
        override fun createFromParcel(parcel: Parcel): MovieParcel {
            return MovieParcel(parcel)
        }

        override fun newArray(size: Int): Array<MovieParcel?> {
            return arrayOfNulls(size)
        }
    }
}