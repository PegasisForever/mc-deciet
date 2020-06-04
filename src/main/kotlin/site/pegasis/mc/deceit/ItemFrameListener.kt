package site.pegasis.mc.deceit

import org.bukkit.*
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.plugin.java.JavaPlugin

class ItemFrameListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEntityEvent) {
        val player = event.player
        if (player.gameMode == GameMode.CREATIVE) return

        if (event.rightClicked is ItemFrame) {
            event.cancel()
            if (player.getGP()?.isInfected == true &&
                Game.started
            ) {
                val itemFrame = event.rightClicked as ItemFrame
                val item = itemFrame.item

                if (item.type == Material.POTION) {
                    BloodPacks.drink(player, itemFrame, plugin)
                } else {
                    player.sendMessage("The blood pack is empty!")
                }
            }
        }
    }
}
