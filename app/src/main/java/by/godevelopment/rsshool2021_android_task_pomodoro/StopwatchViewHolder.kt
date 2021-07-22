package by.godevelopment.rsshool2021_android_task_pomodoro

import android.content.ContentValues.TAG
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.nfc.Tag
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import by.godevelopment.rsshool2021_android_task_pomodoro.databinding.StopwatchItemBinding

// передаем во ViewHolder сгенерированный класс байдинга для разметки элемента RecyclerView.
// Вместо кучи полей для каждого элемента, теперь мы храним один объект со всеми ссылками на элементы-view
// В родительский ViewHolder передаем bindig.root т.е. ссылку на View данного элемента RecyclerView
class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,    // передадим имплементацию интерфейса в качестве параметра в адаптер RecyclerView
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    // Такой подход, когда ViewHolder обрабатывает только визуальное представление айтема, который пришел ему в методе bind, и ничего не меняет напрямую,
    // а все колбэки обрабатываются снаружи (в нашем случае через listener) - является предпочтительным.
    // в метод bind передаем экземпляр Stopwatch, он приходит к нам из метода onBindViewHolder адаптера и содержит актуальные параметры для данного элемента списка.
    fun bind(stopwatch: Stopwatch) {
        val currentTime = SystemClock.uptimeMillis()

        // Элементы нашей item
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.restartButton.text = "Restart"
        val circleProcent = (stopwatch.currentMs / (stopwatch.taskMs / 100)) * 600
        binding.customViewOne.setCurrent(circleProcent)

        // Определяем действия холдера:
        // Если таймер работает,
        if (stopwatch.isStarted) {

            // if () {}
                //         android:background="#E91E63">



            startTimer(stopwatch)   // то рисуем иконки стоп и включаем кастом вью
            Log.i("isStarted?", "startTimer(stopwatch)")
        } else {
            stopTimer(stopwatch)    // иначе, рисуем икноки старт и выключаем кастом вью
            Log.i("isStarted?", "stopTimer(stopwatch)")
        }

        initButtonsListeners(stopwatch) // Настраиваем действия на кнопку-оборотень
    }

    // Настраиваем действия на кнопку-оборотень
    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            // Если таймер работает,
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs) // То на нажатие останавливаем и сохраняем время
                Log.i("isStarted?", "кнопка-оборотень listener.stop")

            } else {
                listener.start(stopwatch.id)    // Иначе, на нажатие запускаем таймер
                Log.i("isStarted?", "кнопка-оборотень listener.start")
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    // Запускаем таймер, рисуем иконку стоп и включаем кастом вью
    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = "Stop"
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

        // в методе startTimer обязательно нужно кэнсельнуть таймер перед созданием нового
        // Это необзодимо по той причине, что RecyclerView переиспользует ViewHolder’ы и один таймер может наложится на другой.
        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()
    }

    // Выключаем таймер и кастом вью, рисуем иконку пуск
    private fun stopTimer(stopwatch: Stopwatch) {

        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        val circleProcent = (stopwatch.currentMs / (stopwatch.taskMs / 100)) * 600
        binding.customViewOne.setCurrent(circleProcent)
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()

        if (stopwatch.currentMs == stopwatch.taskMs) binding.startPauseButton.text = "Start"
        else binding.startPauseButton.text = "Resume"


        if (stopwatch.currentMs == 0L) {
            binding.startPauseButton.visibility = View.GONE
            binding.item.setBackgroundColor(resources.getColor(R.color.finish, null))
        }
        else {
            binding.startPauseButton.visibility = View.VISIBLE
            binding.item.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        timer?.cancel()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs = millisUntilFinished

                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                // Коректируем изображение Кастом-Вью
                val circleProcent = (stopwatch.currentMs / (stopwatch.taskMs / 100)) * 600
                binding.customViewOne.setCurrent(circleProcent)
            }

            override fun onFinish() {
                stopwatch.currentMs = 0L
                listener.stop(stopwatch.id, stopwatch.currentMs)
                stopTimer(stopwatch)
            }
        }
    }

    // данный метод расширения для Long конвертирует текущее значение таймера в миллисекундах в формат “HH:MM:SS:MsMs” и возвращает соответствующую строку
    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        // val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}" // :${displaySlot(ms)}
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00"   // :00
        private const val UNIT_TEN_MS = 1000L
        private const val PERIOD  = 1000L * 60L * 60L * 24L // Day

        private const val INTERVAL_CIRCLE = 100L
        private const val PERIOD_CIRCLE = 1000L * 30 // 30 sec
        private const val REPEAT_CIRCLE = 10 // 10 times
    }

}