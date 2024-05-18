package com.example.mathgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoresAdapter(private val scores: List<Int>) : RecyclerView.Adapter<ScoresAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scoreTextView: TextView = view.findViewById(R.id.score_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.score_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = getOrdinal(position + 1)
        holder.scoreTextView.text = "$place place: ${scores[position]}"
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