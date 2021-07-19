package by.godevelopment.rsshool2021_android_task_pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import by.godevelopment.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import java.time.Duration

class MainActivity : AppCompatActivity(), StopwatchListener {

    private lateinit var binding: ActivityMainBinding


    private val stopwatchAdapter = StopwatchAdapter(this) // Передаем сами себя в val listener: StopwatchListener
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}