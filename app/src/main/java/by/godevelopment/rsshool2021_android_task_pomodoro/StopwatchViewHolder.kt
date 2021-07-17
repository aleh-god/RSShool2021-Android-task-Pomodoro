package by.godevelopment.multiplerowtypesrecyclerview

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import by.godevelopment.multiplerowtypesrecyclerview.databinding.StopwatchItemBinding

// передаем во ViewHolder сгенерированный класс байдинга для разметки элемента RecyclerView.
// Вместо кучи полей для каждого элемента, теперь мы храним один объект со всеми ссылками на элементы-view
// В родительский ViewHolder передаем bindig.root т.е. ссылку на View данного элемента RecyclerView
class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,    // передадим имплементацию интерфейса в качестве параметра в адаптер RecyclerView
    private val resources: Resources
): RecyclerView.ViewHolder(binding.root) {

    // используем Android public abstract class CountDownTimer с простыми методами и калл-бэками
    private var timer: CountDownTimer? = null

    // Такой подход, когда ViewHolder обрабатывает только визуальное представление айтема, который пришел ему в методе bind, и ничего не меняет напрямую,
    // а все колбэки обрабатываются снаружи (в нашем случае через listener) - является предпочтительным.
    // в метод bind передаем экземпляр Stopwatch, он приходит к нам из метода onBindViewHolder адаптера и содержит актуальные параметры для данного элемента списка.
    fun bind(stopwatch: Stopwatch) {
        // Текстовое поле для отображения 00:00:00:00 stopwatch_item
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    // в методе startTimer обязательно нужно кэнсельнуть таймер перед созданием нового.
    // Это необзодимо по той причине, что RecyclerView переиспользует ViewHolder’ы и один таймер может наложится на другой.
    private fun startTimer(stopwatch: Stopwatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    // public abstract class CountDownTimer - значит надо вернуть object этого класса, запихнуть в свойство и вызвать метод класса, через эту переменную
    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
                                    // long millisInFuture, long countDownInterval
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            // Срабатывает калл-бэк на каждом такте таймера. UntilFinished - До завершения
            override fun onTick(millisUntilFinished: Long) {
                // С тактом меняем числа на циферблате, интервалы просчитваем вручную
                stopwatch.currentMs += interval
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }

            // Срабатывает калл-бэк, когда время на таймере закончится
            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
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
        val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD  = 1000L * 60L * 60L * 24L // Day
    }

}