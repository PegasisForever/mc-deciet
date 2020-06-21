package site.pegasis.mc.deceit.gameitem

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.pegasis.mc.deceit.*

class Tracker : GameItem(
    ItemStack(Config.trackerMaterial).apply {
        rename("Tracker")
    }
) {
    var trackJob: Job? = null

    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND || !isHolding()) return
        if (event.player != gp?.player) return
        val targetGp = (event.rightClicked as? Player)?.getGP() ?: return
        targetGp.player.addPotionEffect(
            PotionEffect(
                PotionEffectType.GLOWING,
                10000000,
                1,
                false,
                false,
                true
            )
        )
        trackJob = GlobalScope.launch {
            delay((Config.trackerDuration * 1000).toLong())
            Game.plugin.inMainThread {
                targetGp.player.removePotionEffect(PotionEffectType.GLOWING)
            }
        }
        Game.addListener(GameEvent.ON_END) {
            trackJob?.cancel()
        }

        gp!!.removeGameItem(this)
    }
}
