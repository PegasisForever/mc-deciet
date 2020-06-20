package site.pegasis.mc.deceit.rules

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupArrowEvent
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.cancel

class NoArrowPickupListener : Listener {
    @EventHandler
    fun onArrowPickup(event: PlayerPickupArrowEvent) {
        if (Game.started) event.cancel()
    }
}
