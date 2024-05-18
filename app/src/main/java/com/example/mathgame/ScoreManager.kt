package com.example.mathgame

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ScoreManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    private val gson = Gson()

    fun saveScore(score: Int) {
        val scores = getScores().toMutableSet()
        scores.add(score)
        while (scores.size > 5) {
            scores.remove(scores.minOrNull())
        }
        val sortedScores = scores.sortedDescending()
        val json = gson.toJson(sortedScores)
        editor.putString("scores", json)
        editor.apply()
    }

    fun getScores(): List<Int> {
        val json = sharedPreferences.getString("scores", null)
        val type: Type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}