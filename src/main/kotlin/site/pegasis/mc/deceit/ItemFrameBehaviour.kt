package site.pegasis.mc.deceit

import org.bukkit.*
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin

class ItemFrameBehaviour(val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEntityEvent) {
        val player = event.player
        if (event.rightClicked is ItemFrame &&
            player.gameMode != GameMode.CREATIVE &&
            player.isInfected() &&
            GameState.started
        ) {
            val itemFrame = event.rightClicked as ItemFrame
            val item = itemFrame.item
            event.cancel()

            if (item.type == Material.POTION) {
                BloodPacks.drink(itemFrame)
                GamePlayer.get(player)!!.addBloodLevel(2)
            } else {
                player.sendMessage("The blood pack is empty!")
            }
        }
    }
}
