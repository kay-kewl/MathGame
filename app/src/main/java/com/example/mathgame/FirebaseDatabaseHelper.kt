package com.example.mathgame

import com.google.firebase.database.*

class FirebaseDatabaseHelper {

    private val database: FirebaseDatabase =
        FirebaseDatabase.getInstance("https://math-game-cd4a5-default-rtdb.europe-west1.firebasedatabase.app/")
    private val usersRef: DatabaseReference = database.getReference("users")

    fun getAllUsers(callback: (List<User>) -> Unit) {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = dataSnapshot.children.mapNotNull { it.getValue(User::class.java) }
                callback(users)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun addUser(userId: String, userName: String) {
        val user = mapOf("name" to userName)
        usersRef.child(userId).setValue(user)
    }

    fun getUserName(userId: String, callback: (String) -> Unit) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                callback(user?.name ?: "Unknown")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun updateUserName(userId: String, newUserName: String) {
        usersRef.child(userId).child("name").setValue(newUserName)
    }

    fun addScore(userId: String, score: Int) {
        val totalScoresRef = database.getReference("totalScores")

        totalScoresRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalScores =
                    dataSnapshot.children.mapNotNull { it.getValue(Score::class.java) }
                        .toMutableList()

                val userScoresIndexes = totalScores.withIndex().filter { it.value.userId == userId }.map { it.index }

                if (userScoresIndexes.size == 5 && score <= totalScores[userScoresIndexes.maxOrNull()!!].score!!) {
                    return
                }

                if (totalScores.size < 30 || (totalScores.size == 30 && score > (totalScores.minByOrNull {
                        it.score ?: 0
                    }?.score ?: 0))) {
                    if (totalScores.none { it.score == score }) {
                        val timestamp = System.currentTimeMillis()
                        val scoreData = Score(userId, score, timestamp)

                        if (userScoresIndexes.size == 5) {
                            totalScores[userScoresIndexes.maxOrNull()!!] = scoreData
                        } else {
                            totalScores.add(scoreData)
                        }

                        totalScores.sortByDescending { it.score }
                        val topScores = totalScores.take(30)

                        totalScoresRef.removeValue()

                        topScores.forEachIndexed { index, score ->
                            totalScoresRef.child(index.toString()).setValue(score)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun getTopScores(callback: (MutableList<Score>) -> Unit) {
        val totalScoresRef: DatabaseReference = database.getReference("totalScores")
        totalScoresRef.limitToLast(30).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val topScores = dataSnapshot.children.mapNotNull { snapshot ->
                    snapshot.getValue(Score::class.java)
                }.toMutableList()
                callback(topScores)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun isUserNameTaken(name: String, callback: (Boolean) -> Unit) {
        usersRef.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    callback(dataSnapshot.exists())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors.
                }
            })
    }
}