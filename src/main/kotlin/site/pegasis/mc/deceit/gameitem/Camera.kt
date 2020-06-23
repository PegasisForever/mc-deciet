package site.pegasis.mc.deceit.gameitem

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayerManager

class Camera(amount: Int = 6) : LightSource(
    ItemStack(Config.cameraMaterial).apply {
        this.amount = amount
        rename("Camera")
    }
) {
    private var lastUse: Long = 0

    private fun applyDamage() {
        GamePlayerManager.livingPlayers.forEach { otherGp ->
            if (otherGp == gp) return@forEach
            if (inLightRange(otherGp.player.eyeLocation, Config.cameraAngle, Config.cameraDistance)) {
                launchStunLevelJob(otherGp, 5, 5000L)
            }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (player != gp?.player ||
            event.hand != EquipmentSlot.HAND ||
            !isHolding()
        ) return
        if (System.currentTimeMillis() - lastUse < Config.cameraCoolDown * 1000) return

        setFacingBlockLight(Config.cameraBrightness, Config.cameraDistance)
        applyDamage()
        Game.plugin.runDelayed(Config.cameraDuration) {
            deleteLight(Config.cameraBrightness)
        }

        modifyItemStack {
            amount--
        }
        if (getItemStack() == null) {
            gp!!.removeGameItem(this)
        }
        lastUse = System.currentTimeMillis()
    }
}
