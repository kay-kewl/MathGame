package com.example.mathgame

import AchievementAdapter
import AchievementTracker
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.UUID

class StartMenuActivity : BaseActivity() {
    private lateinit var options: ActivityOptions
    private lateinit var sharedPref: SharedPreferences
    private lateinit var achievementTracker: AchievementTracker
    private lateinit var firebaseDatabaseHelper: FirebaseDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_menu)

        achievementTracker = AchievementTracker(this)

        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        val currentUserName = sharedPref.getString("UserName", null)

        initializeAchievements()
        firebaseDatabaseHelper = FirebaseDatabaseHelper()

        if (currentUserName == null) {
            assignUserName()
        }

        val gameTitle = findViewById<TextView>(R.id.game_title)
        val notificationCountTextView = findViewById<TextView>(R.id.notification_count)
        var newAchievementsCount = getNewAchievementsCount()
        if (newAchievementsCount > 0) {
            notificationCountTextView.text = newAchievementsCount.toString()
            notificationCountTextView.visibility = View.VISIBLE
        } else {
            notificationCountTextView.visibility = View.GONE
        }
        gameTitle.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_achievements)

            dialog.window?.attributes?.dimAmount = 0.0f
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            val display = windowManager.maximumWindowMetrics
            val insets = display.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            displayMetrics.widthPixels = display.bounds.width() - insets.left - insets.right
            displayMetrics.heightPixels = display.bounds.height()

            val window = dialog.window
            val layoutParams = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = displayMetrics.widthPixels
                height = displayMetrics.heightPixels
            }
            window?.attributes = layoutParams

            val achievements = getAchievements()

            val achievementsList = dialog.findViewById<RecyclerView>(R.id.achievements_list)
            achievementsList.layoutManager = LinearLayoutManager(this)
            achievementsList.adapter = AchievementAdapter(this, achievements)

            val closeButton = dialog.findViewById<Button>(R.id.close_button)
            closeButton.background = roundedButtonDrawable
            closeButton.setOnClickListener {
                newAchievementsCount = getNewAchievementsCount()
                if (newAchievementsCount > 0) {
                    notificationCountTextView.text = newAchievementsCount.toString()
                    notificationCountTextView.visibility = View.VISIBLE
                } else {
                    notificationCountTextView.visibility = View.GONE
                }
                dialog.dismiss()
            }

            dialog.show()
        }

        val startButton = findViewById<Button>(R.id.start_button)
        startButton.background = roundedButtonDrawable
        startButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent, options.toBundle())
            finish()
        }

        val scoresButton = findViewById<Button>(R.id.scores_button)
        scoresButton.background = roundedButtonDrawable
        scoresButton.setOnClickListener {
            val intent = Intent(this, ScoresActivity::class.java)
            startActivity(intent, options.toBundle())
            finish()
        }

        val settingsButton = findViewById<Button>(R.id.settings_button)
        settingsButton.background = roundedButtonDrawable
        settingsButton.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_settings)

            dialog.window?.attributes?.dimAmount = 0.0f
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val window = dialog.window
            val layoutParams = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = 950
                height = 1500
            }

            val backButton = dialog.findViewById<Button>(R.id.back_button)
            backButton.background = roundedButtonDrawable
            backButton.setOnClickListener {
                dialog.dismiss()
            }

            val slider = dialog.findViewById<Slider>(R.id.roundness_slider)
            val roundnessText = dialog.findViewById<TextView>(R.id.roundness_text)

            slider.value = buttonRadius
            roundnessText.text = "Button Roundness: ${buttonRadius.toInt()}"

            slider.addOnChangeListener { _, value, _ ->
                roundnessText.text = "Button Roundness: ${value.toInt()}"

                radiusInPixels = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    resources.displayMetrics
                )

                roundedButtonDrawable = createRoundedDrawable(radiusInPixels, Color.BLACK)

                startButton.background = roundedButtonDrawable
                scoresButton.background = roundedButtonDrawable
                settingsButton.background = roundedButtonDrawable
                backButton.background = roundedButtonDrawable

                with (sharedPref.edit()) {
                    putFloat("ButtonRadius", value)
                    apply()
                }

                buttonRadius = value
            }

            val themeSwitch = dialog.findViewById<SwitchMaterial>(R.id.theme_switch)
            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        themeSwitch.isChecked = !isChecked
                    }, 500)

                    achievementTracker.checkAchievement("Yeah, it do be like that sometimes", true)

                }
            }
            window?.attributes = layoutParams
            dialog.show()

            val soundSlider = dialog.findViewById<Slider>(R.id.sound_slider)
            val soundText = dialog.findViewById<TextView>(R.id.sound_text)

            soundSlider.value = sharedPref.getFloat("SoundVolume", 100f)
            soundText.text = "Sound: ${soundSlider.value.toInt()}"

            soundSlider.addOnChangeListener { _, value, _ ->
                soundText.text = "Sound: ${value.toInt()}"

                with (sharedPref.edit()) {
                    putFloat("SoundVolume", value)
                    apply()
                }

                val intent = Intent("CHANGE_VOLUME").apply {
                    putExtra("VOLUME", value / 100f)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
        startService(Intent(this, BackgroundSoundService::class.java))
    }

    private fun initializeAchievements() {
        val achievementsPref = getSharedPreferences("achievements", Context.MODE_PRIVATE)

        if (achievementsPref.getString("achievements", null) == null) {
            val achievements = listOf(
                Achievement(R.drawable.unachieved_achievement2, "Here we go again...", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "Let's do this one last time", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "It's not about victory after all", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "A sign of mastery", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "333", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "Aren't you tired yet?", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "The fastest hand in the wild west", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "1 + 1 = ?", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "Yeah, it do be like that sometimes", "???", false),
                Achievement(R.drawable.unachieved_achievement2, "I am... inevitable", "???", false)
            )

            val editor = achievementsPref.edit()
            editor.putString("achievements", Achievement.listToJson(achievements))
            editor.apply()
        }
    }

    private fun getNewAchievementsCount(): Int {
        val achievements = getAchievements()

        return achievements.count { it.isAchieved && it.isNew }
    }

    private fun getAchievements(): List<Achievement> {
        val achievementsPref = getSharedPreferences("achievements", Context.MODE_PRIVATE)
        val json = achievementsPref.getString("achievements", null)
        return if (json != null) {
            Achievement.jsonToList(json)
        } else {
            listOf()
        }
    }

    private fun assignUserName() {
        firebaseDatabaseHelper.getAllUsers { users ->
            val existingNames = users.map { it.name }
            val newName = generateSequence(1) { it + 1 }
                .map { "Player$it" }
                .first { it !in existingNames }

            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("UserName", newName)
                val userId = UUID.randomUUID().toString()
                putString("UserId", userId)
                firebaseDatabaseHelper.addUser(userId, newName)
                apply()
            }
        }
    }
}