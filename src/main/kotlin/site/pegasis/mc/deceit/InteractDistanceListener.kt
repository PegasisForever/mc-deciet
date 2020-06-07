package site.pegasis.mc.deceit

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class InteractDistanceListener : Listener {
    @EventHandler(priority = EventPriority.LOW)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE || !Game.started) return
        val entity = event.rightClicked
        if (player.location.distanceSquared(entity.location) > Config.interactDistance * Config.interactDistance) {
            event.cancel()
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE || !Game.started) return
        val blockLocation = event.clickedBlock?.location?.clone()?.add(0.5,0.5,0.5)?:return
        if (player.location.distanceSquared(blockLocation) > Config.interactDistance * Config.interactDistance) {
            event.cancel()
        }
    }
}
