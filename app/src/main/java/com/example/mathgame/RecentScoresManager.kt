package com.example.itismydomain

import android.content.Context

class RecentScoresManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("RecentScores", Context.MODE_PRIVATE)

    fun saveScore(score: Int) {
        val scores = getScores().toMutableList()
        scores.add(score)
        while (scores.size > 3) {
            scores.removeAt(0)
        }
        with(sharedPreferences.edit()) {
            putString("scores", scores.joinToString(","))
            apply()
        }
    }

    fun getScores(): List<Int> {
        val scoresString = sharedPreferences.getString("scores", "")
        return scoresString?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
    }
}