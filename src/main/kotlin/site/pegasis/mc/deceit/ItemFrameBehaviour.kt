package site.pegasis.mc.deceit

import org.bukkit.*
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin

class ItemFrameBehaviour(val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEntityEvent) {
        if (event.rightClicked is ItemFrame) {
            val itemFrame = event.rightClicked as ItemFrame
            val player = event.player
            if (player.gameMode == GameMode.CREATIVE) {
                itemFrame.isInvulnerable = true
            } else {
                event.isCancelled = true
                val item = itemFrame.item
                if (item.itemMeta is PotionMeta) {
                    //replace with empty bottle
                    itemFrame.setItem(ItemStack(Material.GLASS_BOTTLE), false)
                    player.world.playSound(
                        itemFrame.location,
                        Sound.ITEM_BUCKET_EMPTY_LAVA,
                        SoundCategory.BLOCKS,
                        1f, 1f
                    )
                } else {
                    //already empty

                }
            }
        }
    }
}
