package site.pegasis.mc.deceit.rules

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerPickupArrowEvent
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.cancel

class NoPickupListener : Listener {
    @EventHandler
    fun onArrowPickup(event: PlayerPickupArrowEvent) {
        if (Game.started) event.cancel()
    }

    @EventHandler
    fun onAttemptItemPickup(event: PlayerAttemptPickupItemEvent) {
        if (Game.started) event.cancel()
    }

    @EventHandler
    fun onItemPickup(event: EntityPickupItemEvent) {
        if (Game.started) event.cancel()
    }
}
