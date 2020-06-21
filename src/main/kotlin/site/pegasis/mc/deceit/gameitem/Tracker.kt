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
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayerEffectFlag
import site.pegasis.mc.deceit.player.GamePlayerManager.gp

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
        val targetGp = (event.rightClicked as? Player)?.gp ?: return
        targetGp.addEffectFlag(GamePlayerEffectFlag.TRACKED, this)
        trackJob = GlobalScope.launch {
            delay((Config.trackerDuration * 1000).toLong())
            Game.plugin.inMainThread {
                targetGp.removeEffectFlag(GamePlayerEffectFlag.TRACKED, this@Tracker)
            }
        }
        Game.addListener(GameEvent.ON_END) {
            trackJob?.cancel()
        }

        gp!!.removeGameItem(this)
    }
}
