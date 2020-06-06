package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

enum class PlayerState {
    NORMAL,
    TRANSFORMED,
    DYING,
    DEAD
}

data class GamePlayer(
    val player: Player,
    val isInfected: Boolean,
    var countDownSecond: Int = 0,
    val scoreboard: Scoreboard = createScoreBoard()
) {
    var bloodLevel: Int = 0
        set(value) {
            field = value.coerceAtMost(6)
            player.exp = field / 6f
        }
    var hasFuse: Boolean = false
        set(value) {
            if (value) {
                addGameItem(GameItem.getFuse())
            } else {
                removeGameItem(GameItem.getFuse())
            }
            field = value
        }
    val glowingEntityIDs: Set<Int>
        get() {
            val set = hashSetOf<Int>()
            if ((Game.state == GameState.DARK || Game.state == GameState.RAGE) && !hasFuse) {
                set += player.world
                    .getEntitiesByClass(FallingBlock::class.java)
                    .filter { it.blockData.material == Config.fuseMaterial }
                    .filter { inHighLightDistance(it) }
                    .map { it.entityId }
            } else if ((Game.state == GameState.DARK || Game.state == GameState.RAGE) && hasFuse) {
                set += player.world
                    .getEntitiesByClass(FallingBlock::class.java)
                    .filter { it.blockData.material == Material.END_PORTAL_FRAME }
                    .filter { inHighLightDistance(it) }
                    .map { it.entityId }
            }
            return set
        }
    var lockGetItem = false
    var rided: Mob? = null // used to let player ride on when dying
    var state = PlayerState.NORMAL
        set(newValue) {
            if (!isInMainThread()) error("Async player state change!")
            if (newValue == field) return

            val plugin = Game.plugin
            if (field == PlayerState.TRANSFORMED && newValue == PlayerState.NORMAL) {
                countDownSecond = 0
                player.removeAllEffect()
                GlobalScope.launch {
                    plugin.changeSkin(player, Config.originalSkinOverride[player.name] ?: player.name)
                }
            } else if (field == PlayerState.NORMAL && newValue == PlayerState.TRANSFORMED) {
                countDownSecond = Config.transformDuration
                GlobalScope.launch {
                    // transform
                    plugin.changeSkin(player, Config.infectedSkin)
                    plugin.inMainThread {
                        clearBloodLevel()
                        player.inventory.heldItemSlot = 8
                        player.addInfectedEffect()
                    }

                    // wait
                    while (countDownSecond > 0) {
                        delay(1000L)
                        countDownSecond--
                    }

                    // back
                    plugin.inMainThread {
                        state = PlayerState.NORMAL
                    }
                }
            } else if (field == PlayerState.NORMAL && newValue == PlayerState.DYING) {
                countDownSecond = Config.playerRespawnDuration

                // sit
                player.health = 1.0
                rided = Bukkit.getWorld(Config.worldName)!!
                    .spawn(player.getUnderBlock().location, Bat::class.java)
                rided!!.isInvulnerable = true
                rided!!.setAI(false)
                rided!!.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false))
                rided!!.addPassenger(player)

                GlobalScope.launch {
                    // wait
                    while (countDownSecond > 0) {
                        delay(1000L)
                        countDownSecond--
                    }

                    // back
                    plugin.inMainThread {
                        state = PlayerState.NORMAL
                    }
                }
            } else if (field == PlayerState.DYING && newValue == PlayerState.NORMAL) {
                player.health = Config.playerRespawnHealth
                rided?.removePassenger(player)
                rided?.remove()
            } else {
                plugin.log("Unknown player ${player.name} transfer: $field to $newValue")
            }
            field = newValue
        }

    init {
        player.scoreboard = scoreboard
    }

    private fun inHighLightDistance(entity: Entity): Boolean {
        return entity.location.distanceSquared(player.location) < Config.highLightDistance * Config.highLightDistance
    }

    fun canTransform() =
        isInfected && ((Game.state == GameState.DARK && bloodLevel == 6) || Game.state == GameState.RAGE)

    private fun clearBloodLevel() {
        bloodLevel = 0
        player.exp = 0f
    }

    private suspend fun JavaPlugin.changeSkin(player: Player, skinName: String) {
        inMainThread { consoleCommand("skin ${player.name} $skinName") }
        delay(300)
        inMainThread { consoleCommand("skinupdate ${player.name}") }
        delay(200)
    }

    private fun Player.addInfectedEffect() {
        addPotionEffect(
            PotionEffect(
                PotionEffectType.NIGHT_VISION,
                10000,
                1
            )
        )
        addPotionEffect(
            PotionEffect(
                PotionEffectType.SPEED,
                10000,
                1
            )
        )
    }

    private fun Player.removeAllEffect() {
        activePotionEffects.forEach { removePotionEffect(it.type) }
    }

    fun addGameItem(item: ItemStack) {
        for (i in 0..8) {
            if (player.inventory.contents[i]?.isSimilar(item) == true) {
                player.inventory.contents[i].amount += item.amount
                return
            }
        }
        for (i in 0..8) {
            if (player.inventory.contents[i] == null) {
                player.inventory.setItem(i, item)
                return
            }
        }
    }

    fun removeGameItem(item: ItemStack) {
        for (i in 0..8) {
            if (player.inventory.contents[i]?.isSimilar(item) == true) {
                player.inventory.setItem(i, null)
                return
            }
        }
    }

    fun resetItemAndState() {
        if (player.gameMode == GameMode.CREATIVE) return
        player.level = 0
        player.exp = 0f
        player.foodLevel = 20
        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        player.inventory.setItemInOffHand(null)
        for (i in 0..8) {
            player.inventory.setItem(i, null)
        }
    }

    companion object {
        val gps = arrayListOf<GamePlayer>()

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
                    gp.resetItemAndState()
                    gp.addGameItem(GameItem.getTransformItem(gp.isInfected))
                    gp.addGameItem(GameItem.getHandGun())
                    gp.addGameItem(GameItem.getAmmo(4))
                    gps += gp
                    if (!debug) {
                        val spawn = Config.spawnPoses.random()
                        player.teleport(player.location.apply { x = spawn.x; y = spawn.y; z = spawn.z })
                    }
                    player.sendTitle(
                        if (gp.isInfected) ChatColor.RED.toString() + "Infected" else "Innocent",
                        "",
                        10,
                        60,
                        10
                    )

                    Game.addListener(GameEvent.ON_SECOND) {
                        updateScoreBoard(gp)
                        if (gp.canTransform()) {
                            player.inventory.contents[0]?.enchant()
                        } else {
                            player.inventory.contents[0]?.removeEnchant()
                        }
                    }
                    Game.addListener(GameEvent.ON_END) {
                        gp.resetItemAndState()
                        updateScoreBoard(gp)
                        gp.state = PlayerState.NORMAL
                        gp.resetItemAndState()
                    }
                }
            }

            Game.addListener(GameEvent.ON_END) {
                gps.clear()
            }
        }

        fun get(player: Player) = gps.find { it.player == player }

        private fun createScoreBoard(): Scoreboard {
            val manager = Bukkit.getScoreboardManager()
            val scoreboard = manager!!.newScoreboard
            val obj = scoreboard.registerNewObjective("game-state", "dummy", "MC Deciet")
            obj.displaySlot = DisplaySlot.SIDEBAR

            return scoreboard
        }

        private val texts = arrayOf(
            "Next Blackout",
            "Enrage in",
            "Time remaining",
            "Go to next area in",
            "Return to human in",
            "Respawn in"
        )

        fun updateScoreBoard(gp: GamePlayer) {
            val obj = gp.scoreboard.objectives.first()
            obj.displayName = when (Game.state) {
                GameState.LIGHT -> "Light On"
                GameState.DARK -> "Blackout"
                GameState.RAGE -> "Enrage"
                GameState.RUN -> "Next Area"
                GameState.END -> "Game ended"
            }

            texts.forEach { gp.scoreboard.resetScores(it) }

            val text = when (Game.state) {
                GameState.LIGHT -> texts[0]
                GameState.DARK -> texts[1]
                GameState.RAGE -> texts[2]
                GameState.RUN -> texts[3]
                GameState.END -> return
            }
            obj.getScore(text).score = Game.secondToNextStage

            if (gp.state == PlayerState.TRANSFORMED) {
                obj.getScore(texts[4]).score = gp.countDownSecond
            } else if (gp.state == PlayerState.DYING) {
                obj.getScore(texts[5]).score = gp.countDownSecond
            }
        }
    }
}

fun Player.getGP() = GamePlayer.get(this)
