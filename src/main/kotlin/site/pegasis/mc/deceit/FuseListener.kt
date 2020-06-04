package site.pegasis.mc.deceit

import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.plugin.java.JavaPlugin

class FuseListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (Game.state!=GameState.DARK && Game.state!=GameState.RAGE) return
        val player = event.player
        val gp = player.getGP() ?: return
        val itemInHand = player.inventory.itemInMainHand
        val entity = event.rightClicked

        if (itemInHand.type != Config.transformMaterial &&
            entity is FallingBlock &&
            entity.blockData.material == Config.fuseMaterial &&
            !gp.hasFuse
        ) {
            FuseManager.getFuse(entity)?.taken = true
            gp.hasFuse = true
        } else if (itemInHand.type == Config.fuseMaterial &&
            entity is FallingBlock &&
            entity.blockData.material == Material.END_PORTAL_FRAME
        ) {
            //todo
            FuseSocketManager.getSocket(entity)?.filled = true
            gp.hasFuse = false
        }
    }
}
