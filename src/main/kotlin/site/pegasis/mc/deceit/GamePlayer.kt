package site.pegasis.mc.deceit

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import com.gmail.filoghost.holographicdisplays.api.line.TextLine
import kotlinx.coroutines.*
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
import org.bukkit.scoreboard.Objective
import kotlin.random.Random
import site.pegasis.mc.deceit.PlayerState.*

enum class PlayerState {
    NORMAL,
    TRANSFORMED,
    VOTING,
    DYING,
    DEAD
}

data class GamePlayer(
    val player: Player,
    val isInfected: Boolean
) {
    var countDownSecond: Int = 0
    var countDownJob: Job? = null
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
    val objective: Objective
    var lockGetItem = false
    var rided: Mob? = null // used to let player ride on when dying
    var hologram: Hologram? = null // used to show vote count when dying
    var hologramVoteLine: TextLine? = null // used to show vote count when dying
    var transformTempItems: List<ItemStack?>? = null
    private var votedGp: HashSet<GamePlayer> = hashSetOf()
    fun vote(gp: GamePlayer) {
        if (state != VOTING || gp in votedGp) return
        votedGp.add(gp)
        hologramVoteLine?.text = ChatColor.GREEN.toString() +
                "⬛".repeat(votedGp.size) +
                ChatColor.GRAY.toString() +
                "⬛".repeat(getRequiredVotes() - votedGp.size)
        if (votedGp.size >= getRequiredVotes()) {
            state = DEAD
        }
    }

    var state = NORMAL
        set(newValue) {
            if (!isInMainThread()) error("Async player state change!")
            if (newValue == field) return

            val plugin = Game.plugin
            if (field == TRANSFORMED && newValue == NORMAL) {
                countDownJob!!.cancel()
                countDownJob = null
                player.removeAllEffect()
                GlobalScope.launch {
                    plugin.changeSkin(player, Config.originalSkinOverride[player.name] ?: player.name)
                    plugin.inMainThread {
                        repeat(9) { player.inventory.setItem(it, transformTempItems!!.get(it)) }
                        transformTempItems = null
                    }
                }
            } else if (field == NORMAL && newValue == TRANSFORMED) {
                countDownSecond = Config.transformDuration
                countDownJob = GlobalScope.launch {
                    // transform
                    plugin.changeSkin(player, Config.infectedSkin)
                    plugin.inMainThread {
                        clearBloodLevel()
                        transformTempItems = player.inventory.contents.take(9).map { it?.clone() }
                        repeat(9) { player.inventory.setItem(it, null) }

                        player.inventory.heldItemSlot = 8
                        player.addInfectedEffect()
                    }

                    // wait
                    waitCountDown()
                    if (!isActive) return@launch

                    // back
                    plugin.inMainThread {
                        state = NORMAL
                    }
                }
            } else if (field == NORMAL && newValue == VOTING) {
                val playerSitPos = player.getUnderBlockLocation()

                countDownSecond = Config.playerRespawnDuration
                votedGp.clear()
                hologram = HologramsAPI.createHologram(
                    Game.plugin,
                    playerSitPos.clone().add(0.0, Config.votingTextHeight, 0.0)
                ).apply {
                    appendTextLine("Voting")
                    hologramVoteLine = appendTextLine(ChatColor.GRAY.toString() + "⬛".repeat(getRequiredVotes()))
                }

                // sit
                player.health = 1.0
                rided = player.world.spawn(playerSitPos, Bat::class.java).apply {
                    isInvulnerable = true
                    setAI(false)
                    addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false))
                    addPassenger(player)
                }

                countDownJob = GlobalScope.launch {
                    // wait
                    waitCountDown()
                    if (!isActive) return@launch

                    // back
                    plugin.inMainThread {
                        state = NORMAL
                    }
                }
            } else if (field == VOTING && newValue == NORMAL) {
                votedGp.clear()
                hologram!!.delete()
                hologram = null

                rided!!.removePassenger(player)
                rided!!.remove()
                rided = null

                player.health = Config.playerRespawnHealth
                player.teleport(Game.level.spawnPoses.random().toLocation())
            } else if (field == VOTING && newValue == DEAD) {
                // todo spectator can't activate listener
                countDownJob!!.cancel()
                votedGp.clear()
                hologram!!.delete()
                hologram = null

                rided!!.removePassenger(player)
                rided!!.remove()
                rided = null

                resetItemAndState()
                player.gameMode = GameMode.SPECTATOR
            } else {
                plugin.log("Unknown player ${player.name} transfer: $field to $newValue")
            }
            field = newValue
        }

    init {
        objective = player.scoreboard.registerNewObjective(Random.nextLong().toString().take(16), "dummy", "MC Deciet")
        objective.displaySlot = DisplaySlot.SIDEBAR
    }

    suspend fun CoroutineScope.waitCountDown() {
        while (countDownSecond > 0 && isActive) {
            delay(1000L)
            countDownSecond--
        }
        if (!isActive) {
            countDownSecond = 0
        }
    }

    private fun inHighLightDistance(entity: Entity): Boolean {
        return entity.location.distanceSquared(player.location) < Config.highLightDistance * Config.highLightDistance
    }

    fun canTransform() =
        isInfected && ((Game.state == GameState.DARK && bloodLevel == 6) || Game.state == GameState.RAGE)

    fun respawn() {
        player.health = if (state == TRANSFORMED) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        } else {
            Config.playerRespawnHealth
        }
        player.teleport(Game.level.spawnPoses.random().toLocation())
    }

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
                1,
                false,
                false,
                false
            )
        )
        addPotionEffect(
            PotionEffect(
                PotionEffectType.SPEED,
                10000,
                1,
                false,
                false,
                false
            )
        )
    }

    fun distanceSquared(other: GamePlayer) = player.location.distanceSquared(other.player.location)

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

    private fun updateScoreBoard() {
        val obj = objective
        val board = obj.scoreboard!!
        obj.displayName = when (Game.state) {
            GameState.LIGHT -> "Light On"
            GameState.DARK -> "Blackout"
            GameState.RAGE -> "Enrage"
            GameState.RUN -> "Next Area"
            GameState.END -> "Game ended"
        }

        val text = when (Game.state) {
            GameState.LIGHT -> "Next Blackout"
            GameState.DARK -> "Enrage in"
            GameState.RAGE -> "Time remaining"
            GameState.RUN -> "Go to next area in"
            GameState.END -> {
                board.entries.forEach { board.resetScores(it) }
                return
            }
        }
        board.entries.filter { it != text }.forEach { board.resetScores(it) }
        obj.getScore(text).score = Game.secondToNextStage

        if (state == PlayerState.TRANSFORMED) {
            obj.getScore("Return to human in").score = countDownSecond
        } else if (state == PlayerState.VOTING) {
            obj.getScore("Respawn in").score = countDownSecond
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
                        gp.updateScoreBoard()
                        if (gp.canTransform()) {
                            player.inventory.contents[0]?.enchant()
                        } else {
                            player.inventory.contents[0]?.removeEnchant()
                        }
                    }
                    Game.addListener(GameEvent.ON_END) {
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

        fun get(player: Player) = gps.find { it.player == player }

        fun livingPlayers() = gps.filter { it.state != PlayerState.DEAD }

        fun getRequiredVotes() = (livingPlayers().size - 1) / 2 + 1
    }
}

fun Player.getGP() = GamePlayer.get(this)
