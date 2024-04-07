package com.example.itismydomain

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScoresActivity : BaseActivity() {
    private lateinit var scoreManager: ScoreManager
    private lateinit var options: ActivityOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)

        options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.background = roundedButtonDrawable
        backButton.setOnClickListener {
            val intent = Intent(this, StartMenuActivity::class.java)
            startActivity(intent, options.toBundle())
            finish()
        }

        scoreManager = ScoreManager(this)
        val scores = scoreManager.getScores()

        val recyclerView = findViewById<RecyclerView>(R.id.scores_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ScoresAdapter(scores)
    }
}