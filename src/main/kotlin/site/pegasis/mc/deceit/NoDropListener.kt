package site.pegasis.mc.deceit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.plugin.java.JavaPlugin

class NoDropListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (GameState.started){
            event.cancel()
        }
    }

    @EventHandler
    fun onClickInventory(event: InventoryClickEvent){
        plugin.log(event)
        if (GameState.started){
            event.cancel()
        }
    }

    @EventHandler
    fun onSwapEvent(event: PlayerSwapHandItemsEvent){
        plugin.log(event)
        if (GameState.started){
            event.cancel()
        }
    }
}
