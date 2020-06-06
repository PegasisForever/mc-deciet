package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team
import org.spigotmc.event.player.PlayerSpawnLocationEvent

class TPLobby(val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onLogin(event: PlayerJoinEvent) {
        event.player.hideNameTag()
    }

    @EventHandler
    fun onLoginLocation(event: PlayerSpawnLocationEvent) {
        if (debug) return
        event.spawnLocation = Config.lobbyLocation.apply { world = Bukkit.getWorld(Config.worldName) }
    }
}
