package za.varsitycollege.kronosapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopwatchService : Service() {
    private var stopwatchTime = 0
    private var stopwatchRunning = false
    private var stopwatchJob: Job? = null
    private var isWorkPhase = true

    private val stopwatchScope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "start" -> {
                val isTesting = intent.getBooleanExtra("isTesting", false)
                val time = intent.getIntExtra("stopwatchTime", 0)
                isWorkPhase = intent.getBooleanExtra("isWorkPhase", true)

                if (isTesting) {
                    val testDuration = 5 // Set the desired test duration in seconds
                    startStopwatch(time, testDuration)
                } else {
                    startStopwatch(time)
                }
            }
            "stop" -> {
                stopStopwatch()
            }
            "reset" -> {
                resetStopwatch()
            }
            else -> {
                val isTesting = intent?.getBooleanExtra("isTesting", false) ?: false
                val time = intent?.getIntExtra("stopwatchTime", 0) ?: 0
                isWorkPhase = intent?.getBooleanExtra("isWorkPhase", true) ?: true

                if (isTesting) {
                    val testDuration = 5 // Set the desired test duration in seconds
                    startStopwatch(time, testDuration)
                } else {
                    startStopwatch(time)
                }
            }
        }

        val notification = NotificationCompat.Builder(this, "StopwatchChannel")
            .setContentTitle("Stopwatch")
            .setContentText("Stopwatch is running")
            .setSmallIcon(R.drawable.kronos_banner)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun startStopwatch(time: Int, duration: Int = 25 * 60) {
        stopwatchTime = time
        stopwatchRunning = true
        stopwatchJob = stopwatchScope.launch {
            while (stopwatchRunning) {
                delay(1000)
                stopwatchTime--
                if (stopwatchTime <= 0) {
                    stopStopwatch()
                    sendStopwatchUpdate()
                    delay(5000) // Delay to allow the dialog to be shown
                    togglePhase()
                    startStopwatch(if (isWorkPhase) 25 * 60 else 5 * 60)
                } else {
                    sendStopwatchUpdate()
                }
            }
        }
    }

    private fun togglePhase() {
        isWorkPhase = !isWorkPhase
        stopwatchTime = if (isWorkPhase) 25 * 60 else 5 * 60
    }

    private fun stopStopwatch() {
        stopwatchRunning = false
        stopwatchJob?.cancel()
        sendStopwatchUpdate() // Add this line to send an update when the stopwatch is stopped
    }

    private fun sendStopwatchUpdate() {
        val intent = Intent("StopwatchUpdate")
        intent.putExtra("stopwatchTime", stopwatchTime)
        intent.putExtra("stopwatchRunning", stopwatchRunning)
        intent.putExtra("isWorkPhase", isWorkPhase)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStopwatch()
    }

    private fun resetStopwatch() {
        stopStopwatch()
        stopwatchTime = 25 * 60
        isWorkPhase = true
        sendStopwatchUpdate()
    }
}
