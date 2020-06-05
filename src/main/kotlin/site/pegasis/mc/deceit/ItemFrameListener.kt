package site.pegasis.mc.deceit

import org.bukkit.*
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.objective.ObjectiveC
import site.pegasis.mc.deceit.objective.ObjectiveManager

class ItemFrameListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEntityEvent) {
        val player = event.player
        val itemFrame = event.rightClicked
        val gp = player.getGP() ?: return
        if (itemFrame !is ItemFrame) return
        if (!Game.started || player.gameMode == GameMode.CREATIVE) return
        event.cancel()

        val item = itemFrame.item
        if (gp.isInfected && item.type == Material.POTION) {
            BloodPacks.drink(player, itemFrame, plugin)
        } else if (gp.isInfected && item.type == Material.GLASS_BOTTLE) {
            player.sendMessage("The blood pack is empty!")
        } else {
            ObjectiveManager.objectives
                .filterIsInstance<ObjectiveC>()
                .any { it.take(itemFrame, gp) }
        }
    }
}
