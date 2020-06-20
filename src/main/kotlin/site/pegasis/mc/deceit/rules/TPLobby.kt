package site.pegasis.mc.deceit.rules

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.debug
import site.pegasis.mc.deceit.hideNameTag

class TPLobby(val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onLogin(event: PlayerJoinEvent) {
        event.player.hideNameTag()
        if (Game.started) {
            event.player.gameMode = GameMode.SPECTATOR
        }
    }

    @EventHandler
    fun onLoginLocation(event: PlayerSpawnLocationEvent) {
        if (debug) return
        event.spawnLocation = Config.lobbyLocation.apply { world = Bukkit.getWorld(
            Config.worldName
        ) }
    }
}
