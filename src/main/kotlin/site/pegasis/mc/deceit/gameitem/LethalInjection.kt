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

class LethalInjection : GameItem(
    ItemStack(Config.lethalInjectionMaterial).apply {
        rename(ChatColor.RED.toString() + "PUFFER FISH")
    }
) {
    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND || !isHolding()) return
        if (event.player != gp?.player) return
        val targetGp = (event.rightClicked as? Player)?.getGP() ?: return
        if (targetGp.state != PlayerState.NORMAL) return

        targetGp.state = PlayerState.DYING
        gp!!.removeGameItem(this)
    }
}
