import android.content.Context
import com.example.itismydomain.Achievement
import com.example.itismydomain.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AchievementTracker(private val context: Context) {
    private val achievementsPref = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = object : TypeToken<List<Achievement>>() {}.type
    private val icons = listOf(
        R.drawable.achievement1,
        R.drawable.achievement2,
        R.drawable.achievement3,
        R.drawable.achievement4,
        R.drawable.achievement5,
        R.drawable.achievement6,
        R.drawable.achievement7,
        R.drawable.achievement8,
        R.drawable.achievement9,
        R.drawable.achievement10
    )
    private val descriptions = listOf(
        "Click Start",
        "Click Try Again",
        "Get score 0",
        "Get the same score bigger than 15 three times in a row",
        "Get Score 3 three times in a row",
        "Play 10 games",
        "Choose a correct answer in less than a second",
        "Get the equation \"1 + 1 = ?\" during the game",
        "Click switch mode in settings",
        "Complete all other achievements"
    )

    fun checkAchievement(achievementName: String, condition: Boolean) {
        if (condition) {
            val achievements = getAchievements()
            val achievement = achievements.find { it.name == achievementName }
            if (achievement != null && !achievement.isAchieved) {
                achievement.isAchieved = true
                achievement.isNew = true
                updateAchievement(achievement)
                checkAllAchievementsAchieved()
            }
        }
    }

    private fun checkAllAchievementsAchieved() {
        val achievements = getAchievements()
        val allAchievementsAchieved = achievements.filter { it.name != "I am... inevitable" }.all { it.isAchieved }
        if (allAchievementsAchieved) {
            val achievement = achievements.find { it.name == "I am... inevitable" }
            if (achievement != null) {
                achievement.isAchieved = true
                achievement.isNew = true
                updateAchievement(achievement)
            }
        }
    }

    private fun getAchievements(): MutableList<Achievement> {
        val jsonAchievements = achievementsPref.getString("achievements", "")
        return if (jsonAchievements.isNullOrEmpty()) {
            mutableListOf()
        } else {
            gson.fromJson(jsonAchievements, type)
        }
    }

    fun updateAchievement(achievement: Achievement) {
        val achievements = getAchievements()
        val index = achievements.indexOfFirst { it.name == achievement.name }
        if (index != -1) {
            achievements[index] = achievement
            achievements[index].icon = if (achievement.isAchieved) icons[index] else R.drawable.unachieved_achievement2
            achievements[index].description = if (achievement.isAchieved) descriptions[index] else "???"
            val editor = achievementsPref.edit()
            editor.putString("achievements", Achievement.listToJson(achievements))
            editor.apply()
        }
    }
}