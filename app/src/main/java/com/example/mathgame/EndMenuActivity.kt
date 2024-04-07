package com.example.itismydomain

import AchievementTracker
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EndMenuActivity : BaseActivity() {
    private lateinit var options: ActivityOptions
    private lateinit var achievementTracker: AchievementTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_menu)

        achievementTracker = AchievementTracker(this)

        options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        val score = intent.getIntExtra("score", 0)
        val scoreTextView = findViewById<TextView>(R.id.current_score_text_view)
        scoreTextView.text = "Score: $score"

        val tryAgainButton = findViewById<Button>(R.id.try_again_button)
        tryAgainButton.background = roundedButtonDrawable
        tryAgainButton.setOnClickListener {
            achievementTracker.checkAchievement("Let's do this one last time", true)

            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent, options.toBundle())
            finish()
        }

        val menuButton = findViewById<Button>(R.id.buttonMenu)
        menuButton.background = roundedButtonDrawable
        menuButton.setOnClickListener {
            val intent = Intent(this, StartMenuActivity::class.java)
            startActivity(intent, options.toBundle())
            finish()
        }

        val scoreManager = ScoreManager(this)
        val scores = scoreManager.getScores().toSet().toList()
        val sortedScores = scores.sortedDescending()

        val scoresAdapter = ScoresAdapter(sortedScores)

        val recyclerView = findViewById<RecyclerView>(R.id.top_scores_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = scoresAdapter
    }
}