package site.pegasis.mc.deceit

import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

object Environment {
    fun hook() {
        GameState.addListener(GameEvent.DARK) {
            consoleCommand("time set midnight")
        }
        GameState.addListener(GameEvent.LIGHT) {
            consoleCommand("time set day")
        }
    }

    suspend fun preStart(plugin: JavaPlugin) {
        if (!debug) {
            repeat(5) { i ->
                plugin.inMainThread {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        player.sendTitle((5 - i).toString(), "", 0, 20, 0)
                    }
                }
                delay(1000)
            }
        }
    }
}
