package com.example.mathgame

import AchievementTracker
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.random.Random

class GameActivity : BaseActivity() {
    private var score = 0
    private var isGameEnded = false
    private var isInvertedMode = false
    private lateinit var equationTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var answerButtons: List<Button>
    private lateinit var timerProgressBar: ProgressBar
    private lateinit var equationGenerator: EquationGenerator
    private lateinit var scoreManager: ScoreManager
    private lateinit var timer: CountDownTimer
    private lateinit var achievementTracker: AchievementTracker
    private lateinit var recentScoresManager: RecentScoresManager
    private lateinit var firebaseDatabaseHelper: FirebaseDatabaseHelper
    private lateinit var options: ActivityOptions
    private var userId: String? = null
    private var userName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        options = ActivityOptions.makeCustomAnimation(this, R.anim.slow_slide_up, R.anim.hold)


        achievementTracker = AchievementTracker(this)
        achievementTracker.checkAchievement("Here we go again...", true)
        recentScoresManager = RecentScoresManager(this)

        firebaseDatabaseHelper = FirebaseDatabaseHelper()

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("UserId", null)
        userName = sharedPref.getString("UserName", null)

        timerProgressBar = findViewById<ProgressBar>(R.id.timer_progress_bar)
        scoreTextView = findViewById<TextView>(R.id.score_text_view)
        equationTextView = findViewById<TextView>(R.id.equation_text_view)
        answerButtons = listOf(
            findViewById<Button>(R.id.answer_button_1),
            findViewById<Button>(R.id.answer_button_2),
            findViewById<Button>(R.id.answer_button_3)
        )

        answerButtons.forEach { button ->
            button.background = roundedButtonDrawable
        }
        equationGenerator = EquationGenerator()
        scoreManager = ScoreManager(this)
        timerProgressBar.max = 3000

        startNewRound()
    }

    private fun startNewRound() {
        val equation = if (isInvertedMode) {
            equationGenerator.generateInvertedEquation(score)
        } else {
            equationGenerator.generateEquation(score)
        }
        equationTextView.text = equation.equation
        if (equation.equation == "1 + 1 = ?") {
            achievementTracker.checkAchievement("1 + 1 = ?", true)
        }
        answerButtons.forEachIndexed { index, button ->
            button.text = equation.answers[index]
            button.setOnClickListener {
                handleAnswerSelected(equation.answers[index], equation.correctAnswer)
            }
        }

        timer = object : CountDownTimer(3000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                timerProgressBar.progress = (3000 - millisUntilFinished).toInt()
            }

            override fun onFinish() {
                if (!isGameEnded) {
                    endGame()
                }
            }
        }.start()
    }

    private fun handleAnswerSelected(selectedAnswer: String, correctAnswer: String) {
        timer.cancel()
        if (selectedAnswer == correctAnswer && !isGameEnded) {
            if (3000 - timerProgressBar.progress < 1000) {
                achievementTracker.checkAchievement("The fastest hand in the wild west", true)
            }
            score++
            scoreTextView.text = "Score: $score"
            val randomChance = Random.nextInt(100) + 1
            if ((!isInvertedMode && randomChance <= 5) || (isInvertedMode && randomChance <= 95)) {
                isInvertedMode = !isInvertedMode
                invertColors()
                answerButtons[2].visibility = if (isInvertedMode) View.INVISIBLE else View.VISIBLE
            }

            startNewRound()
        } else if (!isGameEnded) {
            endGame()
        }
    }

    private fun invertColors() {
        val backgroundColor: Int
        val textColor: Int
        val buttonDrawable: GradientDrawable
        val progressBarDrawable: Int

        if (isInvertedMode) {
            backgroundColor = ContextCompat.getColor(this, R.color.black)
            textColor = ContextCompat.getColor(this, R.color.white)
            progressBarDrawable = R.drawable.custom_progress_bar_inverted
            buttonDrawable = roundedInvertedButtonDrawable
        } else {
            backgroundColor = ContextCompat.getColor(this, R.color.white)
            textColor = ContextCompat.getColor(this, R.color.black)
            progressBarDrawable = R.drawable.custom_progress_bar
            buttonDrawable = roundedButtonDrawable
        }

        findViewById<View>(R.id.constraintLayout).setBackgroundColor(backgroundColor)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        equationTextView.setTextColor(textColor)
        scoreTextView.setTextColor(textColor)

        timerProgressBar.progressDrawable = ContextCompat.getDrawable(this, progressBarDrawable)

        answerButtons.forEach { button ->
            button.setBackgroundColor(textColor)
            button.setTextColor(backgroundColor)
            button.background = buttonDrawable
        }
    }

    private fun endGame() {
        if (!isGameEnded) {
            isGameEnded = true
            scoreManager.saveScore(score)

            val gamesPlayedPref = getSharedPreferences("gamesPlayed", Context.MODE_PRIVATE)
            val gamesPlayed = gamesPlayedPref.getInt("gamesPlayed", 0) + 1

            if (gamesPlayed == 10) {
                achievementTracker.checkAchievement("Aren't you tired yet?", true)
            } else {
                gamesPlayedPref.edit().putInt("gamesPlayed", gamesPlayed).apply()
            }

            recentScoresManager.saveScore(score)
            val lastThreeScores = recentScoresManager.getScores()
            if (lastThreeScores.size >= 3 && lastThreeScores.all { it == 3 }) {
                achievementTracker.checkAchievement("333", true)
            }
            if (lastThreeScores.takeLast(2).all { it > 15 }) {
                achievementTracker.checkAchievement("A sign of mastery", true)
            }
            achievementTracker.checkAchievement("It's not about victory after all", score == 0)

            if (userId != null) {
                firebaseDatabaseHelper.addScore(userId!!, score)
            }

            val intent = Intent(this, EndMenuActivity::class.java)
            intent.putExtra("score", score)
            startActivity(intent, options.toBundle())
            finish()
        }
    }
}