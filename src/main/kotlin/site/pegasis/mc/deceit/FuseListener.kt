package site.pegasis.mc.deceit

import org.bukkit.Material
import org.bukkit.block.data.type.EndPortalFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class FuseListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onLeftClick(event: PlayerInteractEvent) {
        if (!GameState.started) return

        val player = event.player
        val gp = player.getGP() ?: return
        val itemInHand = event.player.inventory.itemInMainHand
        val targetBlock = player.getTargetBlock(null, 4)
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            if (itemInHand.type != Config.transformMaterial &&
                targetBlock.type == Config.fuseMaterial &&
                !gp.hasFuse
            ) {
                targetBlock.type = Material.AIR
                gp.hasFuse = true
            } else if (itemInHand.type == Config.fuseMaterial &&
                targetBlock.type == Material.END_PORTAL_FRAME &&
                !(targetBlock.blockData as EndPortalFrame).hasEye()
            ) {
                targetBlock.blockData = (targetBlock.blockData as EndPortalFrame).apply { setEye(true) }
                gp.hasFuse = false
            }

        }
    }
}
