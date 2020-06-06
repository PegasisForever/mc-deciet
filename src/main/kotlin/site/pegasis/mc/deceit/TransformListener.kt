package site.pegasis.mc.deceit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class TransformListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        val itemInHand = event.player.inventory.itemInMainHand
        val player = event.player
        val gp = player.getGP() ?: return
        if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) &&
            itemInHand.type == Config.transformMaterial &&
            gp.canTransform()
        ) {
            player.getGP()!!.state = PlayerState.TRANSFORMED
        }
    }
}
