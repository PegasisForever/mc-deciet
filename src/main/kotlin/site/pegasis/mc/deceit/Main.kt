package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

val debug = true

open class Main : JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(TPLobby(this), this)
        server.pluginManager.registerEvents(ItemFrameBehaviour(this), this)
        server.pluginManager.registerEvents(Transform(this), this)
        GameState.init(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "start-deciet") {
            GlobalScope.launch {
                startGame()
            }
            return true
        }
        return false
    }

    private suspend fun startGame() {
        GamePlayer.start()
        BloodPacks.loadAll(Bukkit.getWorld(Config.worldName)!!)

        if (!debug) {
            repeat(5) { i ->
                GamePlayer.list.forEach { gp ->
                    gp.player.sendTitle((5 - i).toString(), "", 0, 20, 0)
                }
                delay(1000)
            }
        }

        GamePlayer.list.forEach { gp ->
            gp.player.sendTitle(if (gp.isInfected) "Infected" else "Innocent", "", 10, 60, 10)
        }

        GameState.onDark = {
            consoleCommand("time set midnight")
        }
        GameState.onLight = {
            consoleCommand("time set day")
        }
        GlobalScope.launch { GameState.start() }
    }
}
