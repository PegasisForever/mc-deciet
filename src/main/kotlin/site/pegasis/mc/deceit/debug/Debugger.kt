package site.pegasis.mc.deceit.debug

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.log

object Debugger {
    val player: Player?
        get() = Bukkit.getOnlinePlayers().find { it.name == Config.debuggerName }

    fun actionBar(message: String) {
        player?.sendActionBar(message)
        log("ActionBar: $message")
    }

    fun msg(message: String) {
        player?.sendMessage(message)
        log("Message: $message")
    }

    fun log(message: String) {
        Game.plugin.log(message)
    }
}
