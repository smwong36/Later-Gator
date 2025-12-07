package com.latergator.features.pomodoro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
    PomodoroScreens ViewModel
    - manages the logic for the timer
    - stores the current state of the timers
    - tracks number of sessions and current session

 */
class PomodoroScreens : ViewModel() {

    // -----------------------------
    // Debug Mode to speed up time
    // -----------------------------
    var debugMode = false          // Set to false for real app timing
    private val studySeconds = if (debugMode) 25 else 25 * 60
    private val breakSeconds = if (debugMode) 5 else 5 * 60
    private val tickSpeed = if (debugMode) 200L else 1000L
    // -----------------------------
    // remaining time in seconds of sesssion
    var timeRemaining by mutableStateOf(0)
    // Flags if timer is running
    var isRunning by mutableStateOf(false)
    // Total number of sessions and current session
    var totalSessions by mutableStateOf(0)
    // Current session number and total minutes studied
    var currentSession by mutableStateOf(0)
    // Total minutes studied by user
    var minutesStudied by mutableStateOf(0)
    // Current mode (study or break)
    var mode by mutableStateOf(PomodoroMode.STUDY)
    // Set the number of sessions and reset current session
    fun setSessionCount(count: Int) {
        totalSessions = count
        currentSession = 0
    }
    // Start a new session and start the timer
    fun startStudy() {
        mode = PomodoroMode.STUDY
        timeRemaining = studySeconds
        startTimer()
    }
    // Start a break session and start the timer
    fun startBreak() {
        mode = PomodoroMode.BREAK
        timeRemaining = breakSeconds
        startTimer()
    }
    // Start the timer for the current session
    private fun startTimer() {
        isRunning = true

        viewModelScope.launch {
            while (isRunning && timeRemaining > 0) {
                delay(tickSpeed)
                timeRemaining--

                if (mode == PomodoroMode.STUDY) {
                    minutesStudied++
                }
            }

            isRunning = false
        }
    }
    // Move to the next session and start a new one
    fun moveToNextSession() {
        currentSession++
    }
    // Check if the study cycle is complete
    fun completeStudyCycle(): Boolean {
        return currentSession >= totalSessions
    }
    // Stop the timer
    fun stopTimer() {
        isRunning = false
    }
}
// shows the current state
enum class PomodoroMode { STUDY, BREAK }
