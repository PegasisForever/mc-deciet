package site.pegasis.mc.deceit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

val debug = true

open class Main : JavaPlugin() {
    override fun onEnable() {
        logger.logInfo("Enabled")
        server.pluginManager.registerEvents(TPLobby(this), this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "start-deciet") {
            GlobalScope.launch{
                startGame()
            }
            return true
        }
        return false
    }

    suspend fun startGame() {
        repeat(5){i->
            inMainThread {
                consoleCommand("title @a title {\"text\":\"${5-i}\",\"bold\":true}")
            }
            delay(1.0)
        }
        inMainThread { consoleCommand("title @a reset") }

    }
}
