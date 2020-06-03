package site.pegasis.mc.deceit

import kotlinx.coroutines.delay
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CopyOnWriteArrayList

typealias GameEventListener = JavaPlugin.() -> Unit

enum class GameEvent {
    START,
    LIGHT,
    DARK,
    END,
    SECOND
}

object GameState {
    var started = false
    var dark = false
    var secondToNextStage = 0
    private val listeners = CopyOnWriteArrayList<Pair<GameEvent, GameEventListener>>()
    private lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun addListener(eventType: GameEvent, listener: JavaPlugin.() -> Unit) {
        listeners += (eventType to listener)
    }

    private fun clearListener() {
        listeners.clear()
    }

    private fun dispatch(event: GameEvent) {
        listeners.forEach { (eventType, listener) ->
            if (eventType == event) {
                plugin.inMainThread {
                    listener(plugin)
                }
            }
        }
    }

    suspend fun start() {
        started = true
        dark = false
        secondToNextStage = 10
        dispatch(GameEvent.START)
        dispatch(GameEvent.LIGHT)
        dispatch(GameEvent.SECOND)
        while (secondToNextStage > 0) {
            delay(1000)
            secondToNextStage--
            dispatch(GameEvent.SECOND)
        }

        dark = true
        secondToNextStage = 10
        dispatch(GameEvent.DARK)

        while (secondToNextStage > 0) {
            delay(1000)
            secondToNextStage--
            dispatch(GameEvent.SECOND)
        }
        started = false
        dispatch(GameEvent.LIGHT)
        dispatch(GameEvent.END)
        clearListener()
    }
}
