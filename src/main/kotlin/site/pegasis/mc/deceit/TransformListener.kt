package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class TransformListener(private val plugin: JavaPlugin) : Listener {
    private fun canTransform(gp: GamePlayer) =
        gp.isInfected && ((Game.state == GameState.DARK && gp.bloodLevel == 6) || Game.state == GameState.RAGE)

    @EventHandler
    fun onLeftClick(event: PlayerInteractEvent) {
        val itemInHand = event.player.inventory.itemInMainHand
        val player = event.player
        val gp = player.getGP() ?: return
        if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_AIR) &&
            itemInHand.type == Config.transformMaterial &&
            canTransform(gp)
        ) {
            GlobalScope.launch {
                player.getGP()!!.transform(plugin)
            }
        }
    }
}
