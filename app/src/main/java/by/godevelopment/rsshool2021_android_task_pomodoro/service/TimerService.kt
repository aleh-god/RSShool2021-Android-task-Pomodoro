package by.godevelopment.rsshool2021_android_task_pomodoro.service

import android.os.CountDownTimer
import by.godevelopment.rsshool2021_android_task_pomodoro.databinding.StopwatchItemBinding

class TimerService(
    private val binding: StopwatchItemBinding
    ) {

    val timer = object : CountDownTimer(30000, 1000) {

        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {

        }
    }

}