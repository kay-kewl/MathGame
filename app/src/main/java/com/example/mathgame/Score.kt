package com.example.mathgame

import android.os.Parcel
import android.os.Parcelable

data class Score(
    val userId: String? = null,
    val score: Int? = null,
    val timestamp: Long? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeValue(score)
        parcel.writeValue(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Score> {
        override fun createFromParcel(parcel: Parcel): Score {
            return Score(parcel)
        }

        override fun newArray(size: Int): Array<Score?> {
            return arrayOfNulls(size)
        }
    }
}