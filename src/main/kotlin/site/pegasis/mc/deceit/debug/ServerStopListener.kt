package site.pegasis.mc.deceit.debug

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import site.pegasis.mc.deceit.environment.LightManager

class ServerStopListener : Listener {
    @EventHandler
    fun onPlayerCommand(event: PlayerCommandPreprocessEvent) {
        if (event.message == "/stop" || event.message == "/reload") {
            LightManager.lightOn()
        }
    }
}
