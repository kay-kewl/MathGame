package com.example.mathgame

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScoreInfo(val username: String, val score: Int) : Parcelable