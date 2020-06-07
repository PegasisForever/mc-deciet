package site.pegasis.mc.deceit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupArrowEvent

class NoArrowPickupListener : Listener {
    @EventHandler
    fun onArrowPickup(event: PlayerPickupArrowEvent) {
        if (Game.started) event.cancel()
    }
}
