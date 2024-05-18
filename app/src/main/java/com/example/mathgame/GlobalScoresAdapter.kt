package com.example.mathgame

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch

class GlobalScoresAdapter(private val context: Context, private var scores: MutableList<Pair<String, Int>>) : RecyclerView.Adapter<GlobalScoresAdapter.ViewHolder>() {

    private val firebaseDatabaseHelper = FirebaseDatabaseHelper()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userInfo: TextView = view.findViewById(R.id.user_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.global_score_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = getOrdinal(position + 1)
        holder.userInfo.text = "$place place: ${scores[position].first} - ${scores[position].second}"

        holder.itemView.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_score)
            val userInfoTextView = dialog.findViewById<TextView>(R.id.user_info)
            userInfoTextView.text = scores[position].first
            dialog.show()
        }
    }

    fun updateScores(newScores: List<Score>) {
        CoroutineScope(Dispatchers.IO).launch {
            val newScoreList = MutableList<Pair<String, Int>?>(newScores.size) { null }
            val countDownLatch = CountDownLatch(newScores.size)

            newScores.forEachIndexed { index, score ->
                firebaseDatabaseHelper.getUserName(score.userId ?: "") { userName ->
                    newScoreList[index] = Pair(userName, score.score ?: 0)
                    countDownLatch.countDown()
                }
            }

            countDownLatch.await()

            withContext(Dispatchers.Main) {
                scores = newScoreList.filterNotNull().toMutableList()
                scores.sortByDescending { it.second }

                notifyDataSetChanged()
            }
        }
    }

    fun getUserNameAt(position: Int): String {
        return scores[position].first
    }

    override fun getItemCount() = scores.size

    private fun getOrdinal(n: Int): String {
        val suffixes = arrayOf("th", "st", "nd", "rd")
        return when (n % 100) {
            11, 12, 13 -> n.toString() + suffixes[0]
            else -> {
                when (n % 10) {
                    1 -> n.toString() + suffixes[1]
                    2 -> n.toString() + suffixes[2]
                    3 -> n.toString() + suffixes[3]
                    else -> n.toString() + suffixes[0]
                }
            }
        }
    }
}