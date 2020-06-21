package site.pegasis.mc.deceit.gameitem

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*

class Camera(amount: Int) : GameItem(
    ItemStack(Config.cameraMaterial).apply {
        this.amount = amount
        rename("Camera")
    }
) {
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (player != gp?.player ||
            event.hand != EquipmentSlot.HAND ||
            !isHolding()
        ) return

        val lightBlock = player.rayTraceBlocks(Config.cameraDistance)?.adjacentBlock() ?: player.rayTraceEndBlock(
            Config.cameraDistance
        )
        lightBlock.setLight(Config.cameraBrightness)
        updateLight(player.location)

        Game.plugin.runDelayed(Config.cameraDuration){
            lightBlock.deleteLight()
            updateLight(player.location)
        }

        modifyItemStack {
            amount--
        }
        if (getItemStack() == null) {
            gp!!.removeGameItem(this)
        }
    }
}
