package site.pegasis.mc.deceit

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Pig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class MarkListener : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        val player = event.player
        if (player.gameMode != GameMode.CREATIVE || !marking) return
        val itemInHand = event.player.inventory.itemInMainHand
        if (itemInHand.type != Material.STICK) return
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            val pig = player.world.spawn(player.location, Pig::class.java) {
                globalGlowingIDs.add(it.entityId)
            }
            pig.setAI(false)
            tempPigs.add(pig)
        }
    }
}
