package site.pegasis.mc.deceit.player

import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.gameitem.*

object GamePlayerManager {
    val gps = hashMapOf<Player, GamePlayer>()

    suspend fun preStart(plugin: JavaPlugin) {
        if (!debug) {
            repeat(5) { i ->
                plugin.inMainThread {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        player.sendTitle((5 - i).toString(), "", 0, 20, 0)
                    }
                }
                delay(1000)
            }
        }
    }

    fun hook() {
        Game.addListener(GameEvent.ON_START) {
            val randomInfectedList = listOf(true, true, false, false, false, false).shuffled()
            Bukkit.getOnlinePlayers().take(6).forEachIndexed { i, player ->
                gps[player] = if (debug) {
                    GamePlayer(player, true)
                } else {
                    GamePlayer(player, randomInfectedList[i])
                }
            }
        }

        Game.addListener(GameEvent.ON_END) {
            gps.clear()
        }
    }

    fun getPlayer(name:String)=gps.values.find { it.player.name==name }?.player

    val requiredVotes: Int
        get() = (livingPlayers.size - 1) / 2 + 1

    val livingPlayers: List<GamePlayer>
        get() = gps.values.filter { it.state != PlayerState.DEAD }

    val Player.gp: GamePlayer?
        get() = gps[this]
}
