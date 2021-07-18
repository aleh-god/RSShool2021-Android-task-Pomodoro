package by.godevelopment.rsshool2021_android_task_pomodoro

/*
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.*
import by.godevelopment.foregroundservice.databinding.ActivityMainBinding

// Помечаем Activity интерфейсом-маркером LifecycleObserver - теперь система понимает, что MainActivity наблюдает lifesycle
class MainActivity : AppCompatActivity(), LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // В onCreate() добавляем обсервер ProcessLifecycleOwner.get().lifecycle.addObserver(this), передаем туда this - теперь измененения жизненного цикла будут передаваться в активити, т.е. будут вызываться методы, которые мы пометили соответствующими аннотациями.
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Returns the current time in milliseconds.
        startTime = SystemClock.uptimeMillis()      // Вы должны использовать SystemClock.uptimeMillis()

        lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                binding.timerView.text = (SystemClock.uptimeMillis() - startTime).displayTime()
                delay(INTERVAL)
            }
        }
    }

    // Методы будут вызываться когда соответствующие состояния жизненного цикла будут достигнуты
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
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

 */