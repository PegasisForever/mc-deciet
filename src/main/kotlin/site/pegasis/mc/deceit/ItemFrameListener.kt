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
        if (!Game.started || player.gameMode == GameMode.CREATIVE) return

        if (event.rightClicked is ItemFrame) {
            event.cancel()
            if (player.getGP()?.isInfected == true) {
                val itemFrame = event.rightClicked as ItemFrame
                val item = itemFrame.item

                if (item.type == Material.POTION) {
                    BloodPacks.drink(player, itemFrame, plugin)
                } else if (item.type == Material.GLASS_BOTTLE) {
                    player.sendMessage("The blood pack is empty!")
                }
            }
        }
    }
}
