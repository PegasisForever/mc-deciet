package site.pegasis.mc.deceit

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.CaveSpider
import org.bukkit.entity.Pig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class MarkListener : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        val player = event.player
        if (player.gameMode != GameMode.CREATIVE || !marking) return
        val itemInHand = event.player.inventory.itemInMainHand
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            if (itemInHand.type == Material.STICK) {
                val pig = player.world.spawn(player.location, Pig::class.java)
                pig.setAI(false)
                tempPigs.add(pig)
            }
        }
    }

    @EventHandler
    fun placeSpider(event: PlayerInteractEvent) {
        val player = event.player
        if (player.gameMode != GameMode.CREATIVE) return
        val itemInHand = event.player.inventory.itemInMainHand
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            if (itemInHand.type == Material.EMERALD) {
                val location = player.location
                val spider = player.world.spawn(location, CaveSpider::class.java)
                spider.setAI(false)
                Game.plugin.log("Spider spawn at $location")
            }
        }
    }
}
