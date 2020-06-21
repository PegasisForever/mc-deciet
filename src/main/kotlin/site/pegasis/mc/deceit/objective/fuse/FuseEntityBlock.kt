package site.pegasis.mc.deceit.objective.fuse

import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameState
import site.pegasis.mc.deceit.Main
import site.pegasis.mc.deceit.environment.ConsistentFallingBlock
import site.pegasis.mc.deceit.gameitem.Fuse
import site.pegasis.mc.deceit.player.GamePlayerManager.getGP

class FuseEntityBlock(val block: Block, val fallingBlock: ConsistentFallingBlock) :
    Listener {
    var taken: Boolean = false
        set(value) {
            if (value) {
                fallingBlock.remove()
                FuseManager.availableFuses.remove(this)
                field = value
            }
        }

    init {
        Main.registerEvents(this)
    }

    fun destroy(){
        fallingBlock.remove()
        HandlerList.unregisterAll(this)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (Game.state != GameState.DARK && Game.state != GameState.RAGE) return
        val player = event.player
        val gp = player.getGP() ?: return

        if (event.rightClicked == fallingBlock.block && !gp.hasFuse) {
            taken = true
            gp.addGameItem(Fuse())
        }
    }
}
