package by.godevelopment.rsshool2021_android_task_pomodoro

interface StopwatchListener {

    var globalTime: Long

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun reset(id: Int)

    fun delete(id: Int)
}