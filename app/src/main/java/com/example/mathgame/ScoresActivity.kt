package com.example.mathgame

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScoresActivity : BaseActivity() {
    private lateinit var scoreManager: ScoreManager
    private lateinit var options: ActivityOptions
    private var scores: ArrayList<ScoreInfo>? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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

        val globalScoresButton = findViewById<Button>(R.id.global_scores_button)
        globalScoresButton.background = roundedButtonDrawable
        globalScoresButton.setOnClickListener {
            val intent = Intent(this, GlobalScoresActivity::class.java)
            intent.putParcelableArrayListExtra("globalScores", scores)
            startActivity(intent, options.toBundle())
            finish()
        }

        scoreManager = ScoreManager(this)
        val scores = scoreManager.getScores()

        val recyclerView = findViewById<RecyclerView>(R.id.scores_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ScoresAdapter(scores)

        fetchGlobalScores()
    }

    private fun fetchGlobalScores() {
        coroutineScope.launch {
            val firebaseHelper = FirebaseDatabaseHelper()
            withContext(Dispatchers.IO) {
                firebaseHelper.getTopScores { topScores ->
                    val scoreInfos = ArrayList<ScoreInfo>()
                    for (score in topScores) {
                        val userId = score.userId ?: ""
                        var username = ""
                        firebaseHelper.getUserName(userId) { fetchedUsername ->
                            username = fetchedUsername
                            scoreInfos.add(ScoreInfo(username, score.score ?: 0))
                        }
                    }
                    scores = scoreInfos
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }


}