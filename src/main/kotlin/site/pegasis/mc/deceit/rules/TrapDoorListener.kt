package site.pegasis.mc.deceit.rules

import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.cancel

class TrapDoorListener(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        if (!Game.started || event.player.gameMode == GameMode.CREATIVE) return

        if (event.clickedBlock?.type?.toString()?.endsWith("TRAPDOOR") == true) {
            event.cancel()
        }
    }
}
