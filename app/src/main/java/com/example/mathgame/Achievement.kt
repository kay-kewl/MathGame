package com.example.mathgame

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Achievement(
    var icon: Int,
    val name: String,
    var description: String,
    var isAchieved: Boolean,
    var isNew: Boolean = false
) {
    companion object {
        private val gson = Gson()

        fun listToJson(achievements: List<Achievement>): String {
            return gson.toJson(achievements)
        }

        fun jsonToList(json: String): List<Achievement> {
            val type = object : TypeToken<List<Achievement>>() {}.type
            return gson.fromJson(json, type)
        }
    }
}