package com.latergator.features.pomodoro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PomodoroScreens : ViewModel() {

    // -----------------------------
    // DEBUG MODE TO SPEED UP TIMER!
    // -----------------------------
    var debugMode = true            // ← Set to false for real app timing
    private val studySeconds = if (debugMode) 5 else 25 * 60
    private val breakSeconds = if (debugMode) 3 else 5 * 60
    private val tickSpeed = if (debugMode) 200L else 1000L
    // -----------------------------

    var timeRemaining by mutableStateOf(0)
    var isRunning by mutableStateOf(false)

    var totalSessions by mutableStateOf(1)
    var currentSession by mutableStateOf(1)
    var minutesStudied by mutableStateOf(0)

    var mode by mutableStateOf(PomodoroMode.STUDY)

    fun setSessionCount(count: Int) {
        totalSessions = count
        currentSession = 1
    }

    fun startStudy() {
        mode = PomodoroMode.STUDY
        timeRemaining = studySeconds
        startTimer()
    }

    fun startBreak() {
        mode = PomodoroMode.BREAK
        timeRemaining = breakSeconds
        startTimer()
    }

    private fun startTimer() {
        isRunning = true

        viewModelScope.launch {
            while (isRunning && timeRemaining > 0) {
                delay(tickSpeed)    // ← SPEED ADJUSTED HERE
                timeRemaining--

                if (mode == PomodoroMode.STUDY) {
                    minutesStudied++
                }
            }

            isRunning = false
        }
    }

    fun moveToNextSession() {
        currentSession++
    }

    fun completeStudyCycle(): Boolean {
        return currentSession > totalSessions
    }

    fun stopTimer() {
        isRunning = false
    }
}

enum class PomodoroMode { STUDY, BREAK }
