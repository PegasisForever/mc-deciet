package site.pegasis.mc.deceit

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class FuseListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onLeftClick(event: PlayerInteractEvent) {
        val player = event.player
        val itemInHand = event.player.inventory.itemInMainHand
        val targetBlock = player.getTargetBlock(null, 4)
        if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) &&
            itemInHand.type != Material.ENDER_EYE &&
            targetBlock.type == Material.END_ROD
        ) {
            val gp = player.getGP() ?: return
            if (!gp.hasFuse) {
                targetBlock.type = Material.AIR
                gp.hasFuse = true
            }
        }
    }
}
