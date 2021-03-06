package site.pegasis.mc.deceit.rules

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.cancel

class NoDropListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (!Game.started || event.player.gameMode==GameMode.CREATIVE) return
        event.cancel()
    }

    @EventHandler
    fun onClickInventory(event: InventoryClickEvent) {
        if (!Game.started || event.whoClicked.gameMode==GameMode.CREATIVE) return
        event.cancel()
    }

    @EventHandler
    fun onOpenInventory(event:InventoryOpenEvent){
        if (!Game.started || event.player.gameMode==GameMode.CREATIVE) return
        event.cancel()
    }

    @EventHandler
    fun onSwapEvent(event: PlayerSwapHandItemsEvent) {
        if (!Game.started || event.player.gameMode==GameMode.CREATIVE) return
        event.cancel()
    }
}
