package by.godevelopment.rsshool2021_android_task_pomodoro

import android.os.CountDownTimer

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    val taskMs: Long,
    var isStarted: Boolean,
    var globalMs: Long)
