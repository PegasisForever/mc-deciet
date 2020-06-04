package site.pegasis.mc.deceit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class ServerStopListener : Listener {
    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        if (event.message == "/stop" || event.message == "/reload") {
            Environment.lightOn()
        }
    }
}
