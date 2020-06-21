package site.pegasis.mc.deceit.gameitem

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayerManager.gp

class InspectionKit : GameItem(
    ItemStack(Config.inspectionKitMaterial).apply {
        rename("Inspection Kit")
    }
) {
    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND || !isHolding()) return
        if (event.player != gp?.player) return
        val targetGp = (event.rightClicked as? Player)?.gp ?: return
        val player = gp!!.player

        GlobalScope.launch {
            var periods = 1
            repeat((Config.inspectionKitDelay / 0.2).toInt()) {
                Game.plugin.inMainThread {
                    player.sendActionBar("Inspecting${".".repeat(periods)}")
                }
                periods++
                if (periods > 5) periods = 1
                delay(200)
            }
            Game.plugin.inMainThread {
                player.sendActionBar(
                    if (targetGp.isInfected) {
                        ChatColor.RED.toString() + "${targetGp.player.name} is infected!"
                    } else {
                        ChatColor.GREEN.toString() + "${targetGp.player.name} is innocent!"
                    }
                )
            }
        }

        gp!!.removeGameItem(this)
    }
}
