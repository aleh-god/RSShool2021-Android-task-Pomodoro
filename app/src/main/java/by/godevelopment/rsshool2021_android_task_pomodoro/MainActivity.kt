package by.godevelopment.rsshool2021_android_task_pomodoro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import by.godevelopment.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    // Убрать и заменить секундами из таймера
    private var startTime = 60000L


    private val stopwatchAdapter = StopwatchAdapter(this) // Передаем сами себя в val listener: StopwatchListener
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // В onCreate() добавляем обсервер ProcessLifecycleOwner.get().lifecycle.addObserver(this), передаем туда this - теперь измененения жизненного цикла будут передаваться в активити, т.е. будут вызываться методы, которые мы пометили соответствующими аннотациями.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Это нужно было для приложения, секундомер запускался от старта прилаги
//        startTime = SystemClock.uptimeMillis()      // Вы должны использовать SystemClock.uptimeMillis()
//        lifecycleScope.launch(Dispatchers.Main) {
//            while (true) {
//                // Здесь забивалось основная вьюха задания binding.timerView.text = (SystemClock.uptimeMillis() - startTime).displayTime()
//                delay(INTERVAL)
//            }
//        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val textInput = binding.textInput.text.toString().toIntOrNull()
            if (textInput != null && textInput > 0 && textInput < 1441) {
                val milliSecLong = (textInput * 60000).toLong()

                stopwatches.add(Stopwatch(nextId++, milliSecLong, milliSecLong,false))
                // Хотя использование LiveData <List> - это простой способ предоставить данные адаптеру, это не обязательно - вы можете использовать submitList (List), когда доступны новые списки.
                // Submits a new list to be diffed, and displayed.
                stopwatchAdapter.submitList(stopwatches.toList())
            } else {
                Toast.makeText(this, "введите значение от 1 до 1440", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Создаём соответствующий интерфейс, имплементируем который в MainActivity (поскольку именно в этом классе у нас логика управления списком таймеров), и передадим эту имплементацию в качестве параметра в адаптер RecyclerView:
    override fun start(id: Int) {
        // changeStopwatch(id, null, true)

        val newTimers = mutableListOf<Stopwatch>()

        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.currentMs, it.taskMs, true))
            } else {
                newTimers.add(Stopwatch(it.id, it.currentMs, it.taskMs, false))
                // newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)

    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun reset(id: Int) {
        // changeStopwatch(id, null, false)
        
        val newTimers = mutableListOf<Stopwatch>()

        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.taskMs, it.taskMs, it.isStarted))
            } else {
                newTimers.add(Stopwatch(it.id, it.currentMs, it.taskMs, it.isStarted))
                // newTimers.add(it)
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

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        // Не изменяем текущий, а пропускаем, вместо - записываем новый со старым id
        // Остальные переписываем
        stopwatches.forEach {
            if (it.id == id) {
                // Если null, то запускаем с последнего значенияч
                newTimers.add(Stopwatch(it.id, currentMs ?: it.currentMs, it.taskMs, isStarted))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    // Методы будут вызываться когда соответствующие состояния жизненного цикла будут достигнуты
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
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

    private companion object {

        private const val INTERVAL = 10L
    }
}