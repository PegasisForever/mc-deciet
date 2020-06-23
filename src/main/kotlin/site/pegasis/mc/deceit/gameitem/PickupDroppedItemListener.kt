package site.pegasis.mc.deceit.gameitem

import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.environment.DroppedItemManager
import site.pegasis.mc.deceit.environment.DroppedItemManager.getGameItem
import site.pegasis.mc.deceit.getNearbyEntities
import site.pegasis.mc.deceit.player.GamePlayerManager.gp

class PickupDroppedItemListener : Listener {
    @EventHandler
    fun onCrouch(event: PlayerToggleSneakEvent) {
        if (!Game.started) return
        if (!event.isSneaking) return
        val gp = event.player.gp ?: return
        val item = Game.world.getNearbyEntities(gp.player.location, Config.playerPickupDistance)
            .filterIsInstance<Item>()
            .firstOrNull { DroppedItemManager.isRandomDroppedItem(it.itemStack.type) } ?: return
        val gameItem = item.getGameItem()

        gp.addGameItem(gameItem)
        item.remove()
    }
}
