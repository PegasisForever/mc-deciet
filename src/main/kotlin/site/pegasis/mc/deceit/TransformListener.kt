package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class TransformListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onLeftClick(event: PlayerInteractEvent) {
        val itemInHand = event.player.inventory.itemInMainHand
        val player = event.player
        if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_AIR) &&
            itemInHand.type == Config.transformMaterial &&
            GameState.dark &&
            GameState.started &&
            player.getGP()?.isInfected == true &&
            player.getGP()?.bloodLevel == 6
        ) {
            GlobalScope.launch {
                player.getGP()!!.transform(plugin)
            }
        }
    }
}
