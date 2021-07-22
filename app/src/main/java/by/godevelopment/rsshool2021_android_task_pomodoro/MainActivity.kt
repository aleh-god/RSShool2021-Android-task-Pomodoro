package by.godevelopment.rsshool2021_android_task_pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import by.godevelopment.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import by.godevelopment.rsshool2021_android_task_pomodoro.foregroundService.COMMAND_ID
import by.godevelopment.rsshool2021_android_task_pomodoro.foregroundService.COMMAND_START
import by.godevelopment.rsshool2021_android_task_pomodoro.foregroundService.COMMAND_STOP
import by.godevelopment.rsshool2021_android_task_pomodoro.foregroundService.STARTED_TIMER_TIME_MS

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this) // Передаем сами себя в val listener: StopwatchListener
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    // private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // В onCreate() добавляем обсервер ProcessLifecycleOwner.get().lifecycle.addObserver(this), передаем туда this - теперь измененения жизненного цикла будут передаваться в активити, т.е. будут вызываться методы, которые мы пометили соответствующими аннотациями.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val textInput = binding.textInput.text.toString().toIntOrNull()
            if (textInput != null && textInput > 0 && textInput < 1441) {
                val milliSecLong = (textInput * 60000).toLong()

                stopwatches.add(Stopwatch(nextId++, milliSecLong, milliSecLong,false,0L))
                // Хотя использование LiveData <List> - это простой способ предоставить данные адаптеру, это не обязательно - вы можете использовать submitList (List), когда доступны новые списки.
                // Submits a new list to be diffed, and displayed.
                stopwatchAdapter.submitList(stopwatches.toList())
            } else {
                Toast.makeText(this, "введите значение от 1 до 1440", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Реализация интерфейса

    // Запускаем таймер, остальные останавливаем и добавляем глобальное время для коррекции
    override fun start(id: Int) {
        val newTimers = mutableListOf<Stopwatch>()

        stopwatches.forEach {
            // Не изменяем текущий, а вместо - записываем в новый лист со старым id
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.currentMs, it.taskMs, true, SystemClock.uptimeMillis()))

            } else {
                // Возможно здесь занулять не надо или проводить коррекцию
                newTimers.add(Stopwatch(it.id, it.currentMs, it.taskMs, false, 0L))
            }
        }

        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    // Останавливаем и изменяем таймер, добавляем глобальное время для коррекции
    override fun stop(id: Int, currentMs: Long) {
        val newTimers = mutableListOf<Stopwatch>()

        stopwatches.forEach {
            if (it.id == id) {
                                                    // Если null, то запускаем с последнего значенияч
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, it.taskMs, false, 0L))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    override fun reset(id: Int) {
        val newTimers = mutableListOf<Stopwatch>()

        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.taskMs, it.taskMs, false, 0L))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)

    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    // Методы будут вызываться когда соответствующие состояния жизненного цикла будут достигнуты
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val activeWatches = stopwatches.find { it.isStarted == true }
        val startTime = activeWatches?.currentMs ?: 0L

        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime) // вставить секунды из таймера
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

//    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
//        return object : CountDownTimer(stopwatch.currentMs, UNIT_TEN_MS) {
//            val interval = UNIT_TEN_MS
//
//            override fun onTick(millisUntilFinished: Long) {
//                // Листаем лист, берем объект с инСтарт, чекаем его на глобальное время, минусуем сикунду
//                // Сообщение о том что таймер закончился
//                // stopwatch.currentMs += interval
//                // binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
//            }
//
//            override fun onFinish() {
//                // binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
//            }
//        }
//    }

//    private companion object {
//
//        private const val UNIT_TEN_MS = 10L
//        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
//    }
}