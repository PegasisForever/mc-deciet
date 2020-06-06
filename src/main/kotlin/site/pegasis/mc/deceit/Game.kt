package site.pegasis.mc.deceit

import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CopyOnWriteArrayList

typealias GameEventListener = JavaPlugin.() -> Unit

enum class GameEvent {
    ON_START,
    ON_LIGHT_ON,
    ON_LIGHT_OFF,
    ON_LIGHT,
    ON_DARK,
    ON_RAGE,
    ON_RUN,
    ON_END,
    ON_SECOND,
    ON_LEVEL_START,
    ON_LEVEL_END
}

enum class GameState {
    END,
    LIGHT,
    DARK,
    RAGE,
    RUN // to next area
}

object Game {
    var state = GameState.END
    val started: Boolean
        get() = state != GameState.END
    var secondToNextStage = 0
    var levelIndex = 0
    var level = Config.levels[levelIndex]
    private val listeners = CopyOnWriteArrayList<Pair<GameEvent, GameEventListener>>()
    lateinit var plugin: JavaPlugin
    lateinit var world: World

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
        world = Bukkit.getWorld(Config.worldName)!!
    }

    fun addListener(eventType: GameEvent, listener: JavaPlugin.() -> Unit) {
        listeners += (eventType to listener)
    }

    fun log(msg:Any?){
        plugin.log(msg)
    }

    private fun clearListener() {
        listeners.clear()
    }

    private suspend fun dispatch(event: GameEvent) {
        listeners.forEach { (eventType, listener) ->
            if (eventType == event) {
                plugin.inMainThread {
                    listener(plugin)
                }
            }
        }
    }

    private fun fuseFilled() = FuseSocketManager.filledSockets >= level.requiredFuses

    private suspend fun endGame() {
        dispatch(GameEvent.ON_LIGHT_ON)
        state = GameState.END
        dispatch(GameEvent.ON_END)
        clearListener()
    }

    suspend fun start() {
        for (levelIndex in Config.levels.indices) {
            this.levelIndex = levelIndex
            level = Config.levels[Game.levelIndex]

            // light
            state = GameState.LIGHT
            secondToNextStage = level.lightTime
            dispatch(GameEvent.ON_START)
            dispatch(GameEvent.ON_LEVEL_START)
            dispatch(GameEvent.ON_LIGHT)
            dispatch(GameEvent.ON_LIGHT_ON)
            dispatch(GameEvent.ON_SECOND)
            waitUntilNextStageIf()

            // dark
            state = GameState.DARK
            secondToNextStage = level.darkTime
            dispatch(GameEvent.ON_DARK)
            dispatch(GameEvent.ON_LIGHT_OFF)
            waitUntilNextStageIf { !fuseFilled() }

            // rage
            if (!fuseFilled()) {
                state = GameState.RAGE
                secondToNextStage = level.rageTime
                dispatch(GameEvent.ON_RAGE)
                dispatch(GameEvent.ON_LIGHT_OFF)
                waitUntilNextStageIf { !fuseFilled() }
                if (!fuseFilled()) {
                    plugin.log("infector win")
                    dispatch(GameEvent.ON_LEVEL_END)
                    endGame()
                    return
                }
            }

            // run
            state = GameState.RUN
            secondToNextStage = level.runTime
            dispatch(GameEvent.ON_RUN)
            dispatch(GameEvent.ON_LIGHT_ON)
            waitUntilNextStageIf()

            dispatch(GameEvent.ON_LEVEL_END)
        }

        plugin.log("innocent win")
        endGame()
    }

    private suspend fun waitUntilNextStageIf(keepIf: (() -> Boolean)? = null) {
        while (secondToNextStage > 0) {
            delay(1000)
            secondToNextStage--
            dispatch(GameEvent.ON_SECOND)
            if (keepIf?.invoke() == false) {
                secondToNextStage = 0
            }
        }
    }
}
