package com.example.itismydomain

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Achievement(
    var icon: Int,  // current icon id
    val name: String,  // name of the achievement
    var description: String,  // description of the achievement
    var isAchieved: Boolean,  // whether the achievement is achieved
    var isNew: Boolean = false  // whether the achievement is new
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