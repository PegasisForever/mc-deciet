package site.pegasis.mc.deceit.gameitem

import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.objective.fuse.FuseSocketManager

class Fuse : GameItem(
    ItemStack(Config.fuseMaterial).apply {
        rename("Fuse")
    }
) {
    var used = false

    override fun onAttach(gp: GamePlayer, index: Int) {
        super.onAttach(gp, index)
        Game.addListener(GameEvent.ON_LEVEL_END) {
            gp.removeGameItem(this@Fuse)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (Game.state != GameState.DARK && Game.state != GameState.RAGE) return
        if (event.player != gp?.player) return
        if (event.player.inventory.itemInMainHand != getItemStack()) return
        val entity = event.rightClicked

        if (entity is FallingBlock &&
            entity.blockData.material == Material.END_PORTAL_FRAME
        ) {
            FuseSocketManager.getSocket(entity)?.filled = true
            used = true
            gp!!.removeGameItem(this)
            HandlerList.unregisterAll(this)
        }
    }
}
