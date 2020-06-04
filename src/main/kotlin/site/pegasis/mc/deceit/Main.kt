package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

val debug = true

open class Main : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(TPLobby(this), this)
        server.pluginManager.registerEvents(ItemFrameListener(this), this)
        server.pluginManager.registerEvents(TransformListener(this), this)
        server.pluginManager.registerEvents(FuseListener(this), this)
        server.pluginManager.registerEvents(NoDropListener(this), this)
        server.pluginManager.registerEvents(ServerStopListener(), this)
        GameState.init(this)
    }

    override fun onDisable() {
        super.onDisable()
    }
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "start-deciet") {
            GlobalScope.launch {
                startGame()
            }
            return true
        } else if (command.name == "light-off") {
            Environment.lightOff()
            return true
        } else if (command.name == "light-on") {
            Environment.lightOn()
            return true
        }
        return false
    }

    private suspend fun startGame() {
        GamePlayer.preStart(this)

        GamePlayer.hook()
        BloodPacks.hook()
        Environment.hook()

        GameState.start()
    }
}
