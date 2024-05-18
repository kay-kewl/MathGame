package com.example.mathgame

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlobalScoresActivity : BaseActivity() {
    private lateinit var databaseHelper: FirebaseDatabaseHelper
    private lateinit var options: ActivityOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_scores)

        options = ActivityOptions.makeCustomAnimation(this, 0, 0)

        databaseHelper = FirebaseDatabaseHelper()

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val hasBeenAsked = sharedPref.getBoolean("HasBeenAsked", false)
        val currentUserName = sharedPref.getString("UserName", "Error")
        val recyclerView = findViewById<RecyclerView>(R.id.global_scores_recycler_view)

        val scores: ArrayList<ScoreInfo>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("globalScores", ScoreInfo::class.java)
        } else {
            intent.getParcelableArrayListExtra("globalScores")
        }

        val scoresList: MutableList<Pair<String, Int>> = scores?.map { Pair(it.username ?: "", it.score ?: 0) }?.toMutableList() ?: mutableListOf()

        scoresList.sortByDescending { it.second }

        val adapter = GlobalScoresAdapter(this, scoresList)

        if (!hasBeenAsked) {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_enter_name)

            dialog.window?.attributes?.dimAmount = 0.0f
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            val display = windowManager.maximumWindowMetrics
            val insets =
                display.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            displayMetrics.widthPixels = display.bounds.width() - insets.left - insets.right
            displayMetrics.heightPixels = display.bounds.height()

            val window = dialog.window
            val layoutParams = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                this.width = 1000
                this.height = 1400
            }

            val saveButton = dialog.findViewById<Button>(R.id.save_button)
            saveButton.background = roundedButtonDrawable
            val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
            cancelButton.background = roundedButtonDrawable
            val nameEditText = dialog.findViewById<EditText>(R.id.username_edit_text)
            nameEditText.setText(currentUserName)

            saveButton.setOnClickListener {
                val enteredName = nameEditText.text.toString()
                if (isValidName(enteredName)) {
                    databaseHelper.isUserNameTaken(enteredName) { isTaken ->
                        if (isTaken) {
                            Toast.makeText(this, "This name is already taken", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val currentUserId = sharedPref.getString("UserId", null)
                            if (currentUserId != null) {
                                databaseHelper.updateUserName(currentUserId, enteredName)
                                databaseHelper.getTopScores { topScores ->
                                    adapter.updateScores(topScores)
                                }
                                with(sharedPref.edit()) {
                                    putString("UserName", enteredName)
                                    putBoolean("HasBeenAsked", true)
                                    apply()
                                }
                                dialog.dismiss()
                            } else {
                                Toast.makeText(this, "Error updating username", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                } else {
                    nameEditText.error = "English letters, numbers, spaces and some special symbols are allowed. Max length is 10."
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            window?.attributes = layoutParams
            dialog.show()
        }

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.background = roundedButtonDrawable
        backButton.setOnClickListener {
            val intent = Intent(this, ScoresActivity::class.java)
            startActivity(intent, options.toBundle())
            finish()
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseHelper.getTopScores { topScores ->
            adapter.updateScores(topScores)
            recyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                    this,
                    recyclerView,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        override fun onItemClick(view: View, position: Int) {
                            val scoreData = topScores[position]
                            val dialog = Dialog(this@GlobalScoresActivity)
                            dialog.setContentView(R.layout.dialog_score_details)

                            dialog.window?.attributes?.dimAmount = 0.0f
                            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
                            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation2

                            val scoreTextView = dialog.findViewById<TextView>(R.id.score_text_view)
                            val userNameTextView = dialog.findViewById<TextView>(R.id.user_name_text_view)
                            val dateTextView = dialog.findViewById<TextView>(R.id.date_text_view)
                            val closeButton = dialog.findViewById<Button>(R.id.close_button)

                            scoreTextView.text = getString(R.string.score_text, scoreData.score)
                            userNameTextView.text = getString(R.string.username_text, adapter.getUserNameAt(position))
                            dateTextView.text = getString(R.string.date_text, SimpleDateFormat("dd 'of' MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(scoreData.timestamp ?: 0L)))

                            closeButton.setOnClickListener {
                                dialog.dismiss()
                            }

                            dialog.show()
                        }

                        override fun onLongItemClick(view: View?, position: Int) {
                            // Do nothing on long click.
                        }

                    })
            )
        }
    }

    private fun isValidName(name: String): Boolean {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_()-.!@#$%*{}[] "
        return name.isNotBlank() &&
                name.length <= 10 &&
                name.all { it in allowedChars }
    }
}