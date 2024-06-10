package za.varsitycollege.kronosapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TimerManager {
    private val timers = mutableStateListOf<Timer>()
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun addTimer(timer: Timer, categoryManager: CategoryManager) {
        timers.add(timer)
        val userId = auth.currentUser?.uid ?: return

        val timerData = timer.toFirestoreMap()
        firestore.collection("users").document(userId).collection("timers")
            .add(timerData)
            .addOnSuccessListener { documentReference ->
                // Successfully added the timer
            }
            .addOnFailureListener { e ->
                // Handle the error
            }

        // Update category hours
        val category = categoryManager.getCategories().find { it.name == timer.category }
        category?.totalHours = category?.totalHours?.plus(timer.hours) ?: 0f
    }

    fun getTimers(): List<Timer> {
        return timers
    }

    fun fetchTimers(onFetchComplete: (Boolean, Exception?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFetchComplete(false, Exception("User not logged in"))
            return
        }
        firestore.collection("users").document(userId).collection("timers")
            .get()
            .addOnSuccessListener { documents ->
                timers.clear()
                for (document in documents) {
                    val timer = document.toObject(Timer::class.java)
                    timer.image = timer.imageBase64?.base64ToBitmap()
                    timers.add(timer)
                }
                onFetchComplete(true, null)
            }
            .addOnFailureListener { e ->
                onFetchComplete(false, e)
            }
    }
}