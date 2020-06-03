package site.pegasis.mc.deceit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class TPLobby(val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player
        if (player.name == "Pegasis" && debug) return

        plugin.runDelayed(0.5) {
            player.teleport(Config.lobbyLocation.apply { world = player.world })
            plugin.logger.logInfo("Tped ${player.name} to lobby.")
        }
    }
}
