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

object GamePlayerManager{
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
                val gp = if (debug) {
                    GamePlayer(player, true)
                } else {
                    GamePlayer(player, randomInfectedList[i])
                }
                if (!debug) {
                    player.gameMode = GameMode.ADVENTURE
                }
                gp.resetItemAndState()
                gp.addGameItem(TransformItem(gp.isInfected))
                gp.addGameItem(Crossbow())
                gp.addGameItem(Arrow(4))
                gp.addGameItem(LethalInjection())
                gp.addGameItem(Antidote())
                gp.addGameItem(Torch(64))
                gp.addGameItem(Torch(64))
                gps[player] = gp
                if (!debug) {
                    val spawn = Game.level.spawnPoses.random()
                    player.teleport(player.location.apply { x = spawn.x; y = spawn.y; z = spawn.z })
                }
                server.pluginManager.registerEvents(gp, Game.plugin)
                player.sendTitle(
                    if (gp.isInfected) ChatColor.RED.toString() + "Infected" else "Innocent",
                    "",
                    10,
                    60,
                    10
                )

                Game.addListener(GameEvent.ON_SECOND) {
                    gp.updateScoreBoard()
                    if (gp.canTransform()) {
                        player.inventory.contents[0]?.enchant()
                    } else {
                        player.inventory.contents[0]?.removeEnchant()
                    }
                }
                Game.addListener(GameEvent.ON_END) {
                    HandlerList.unregisterAll(gp)
                    if (gp.torchLightBlock != null) {
                        gp.torchLightBlock!!.deleteLight()
                        gp.torchLightBlock = null
                        updateLight(gp.player.location)
                    }
                    gp.resetItemAndState()
                    gp.updateScoreBoard()
                    gp.state = PlayerState.NORMAL
                    gp.resetItemAndState()
                }
            }
        }

        Game.addListener(GameEvent.ON_END) {
            gps.clear()
        }
    }

    fun livingPlayers() = gps.values.filter { it.state != PlayerState.DEAD }

    fun getRequiredVotes() = (livingPlayers().size - 1) / 2 + 1

    fun Player.getGP() = gps[player]
}
