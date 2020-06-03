package site.pegasis.mc.deceit

import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard


object GameState {
    var started = false
    var dark = false
    var secondToNextStage = 0
    lateinit var plugin: JavaPlugin
    lateinit var scoreboard: Scoreboard
    var onDark: (() -> Unit)? = null
    var onLight: (() -> Unit)? = null
    var onEnd: (() -> Unit)? = null

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    suspend fun start() {
        started = true
        dark = false
        secondToNextStage = 10
        createScoreBoard()
        updateScoreBoard()
        plugin.inMainThread { onLight?.invoke() }
        while (secondToNextStage > 0) {
            delay(1000)
            secondToNextStage--
            updateScoreBoard()
        }
        dark = true
        plugin.inMainThread { onDark?.invoke() }

        secondToNextStage = 10
        while (secondToNextStage > 0) {
            delay(1000)
            secondToNextStage--
            updateScoreBoard()
        }
        started = false
        updateScoreBoard()
        plugin.inMainThread {
            onLight?.invoke()
            onEnd?.invoke()
        }
    }

    private fun createScoreBoard() {
        plugin.inMainThread {
            val manager = Bukkit.getScoreboardManager()
            scoreboard = manager!!.newScoreboard
            val obj = scoreboard.registerNewObjective("game-state", "dummy", "MC Deciet")
            obj.displaySlot = DisplaySlot.SIDEBAR

            GamePlayer.list.forEach { gp ->
                gp.player.scoreboard = scoreboard
            }
        }
    }

    private fun updateScoreBoard() {
        plugin.inMainThread {
            val obj = scoreboard.objectives.first()
            obj.displayName = when {
                !started -> "Game End"
                dark -> "Dark"
                else -> "Light"
            }

            obj.getScore("Seconds left").score = secondToNextStage
        }
    }
}
