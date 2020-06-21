package site.pegasis.mc.deceit.gameitem

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.PlayerState
import site.pegasis.mc.deceit.getGP
import site.pegasis.mc.deceit.rename

class Antidote:GameItem(
    ItemStack(Config.antidoteMaterial).apply {
        rename("Antidote")
    }
){
    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND || !isHolding()) return
        if (event.player != gp?.player) return
        val targetGp = (event.rightClicked as? Player)?.getGP() ?: return
        if (targetGp.state != PlayerState.DYING) return

        targetGp.state = PlayerState.NORMAL
        gp!!.removeGameItem(this)
    }
}
