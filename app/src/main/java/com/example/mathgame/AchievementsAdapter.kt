import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itismydomain.Achievement
import com.example.itismydomain.R

class AchievementAdapter(private val context: Context, private val achievements: List<Achievement>) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    private val achievementTracker = AchievementTracker(context)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.achievement_icon)
        val nameTextView: TextView = itemView.findViewById(R.id.achievement_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.achievement_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.iconImageView.setImageResource(achievement.icon)
        holder.nameTextView.text = achievement.name

        holder.itemView.setOnClickListener {
            val dialog = Dialog(context)
            dialog.window?.attributes?.dimAmount = 0.0f
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            dialog.setContentView(R.layout.dialog_achievement)
            achievement.isNew = false
            achievementTracker.updateAchievement(achievement)

            val nameTextView = dialog.findViewById<TextView>(R.id.name)
            val iconImageView = dialog.findViewById<ImageView>(R.id.icon)
            val descriptionTextView = dialog.findViewById<TextView>(R.id.description)
            val closeButton = dialog.findViewById<Button>(R.id.close_achievement_button)

            nameTextView.text = achievement.name
            iconImageView.setImageResource(achievement.icon)
            descriptionTextView.text = achievement.description

            closeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount() = achievements.size
}