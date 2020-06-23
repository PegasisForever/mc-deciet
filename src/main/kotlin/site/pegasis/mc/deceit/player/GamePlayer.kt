package site.pegasis.mc.deceit.player

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import com.gmail.filoghost.holographicdisplays.api.line.TextLine
import kotlinx.coroutines.*
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.debug.Debugger
import site.pegasis.mc.deceit.gameitem.*
import site.pegasis.mc.deceit.gameitem.Arrow
import site.pegasis.mc.deceit.player.GamePlayerManager.gps
import site.pegasis.mc.deceit.player.GamePlayerManager.livingPlayers
import site.pegasis.mc.deceit.player.GamePlayerManager.requiredVotes
import site.pegasis.mc.deceit.player.PlayerState.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class GamePlayer(
    val player: Player,
    val isInfected: Boolean
) : Listener {
    private val scoreboardObjective: Objective
    private var rided: Mob? = null // used to let player ride on when dying
    private var hologram: Hologram? = null // used to show vote count when dying
    private var hologramVoteLine: TextLine? = null // used to show vote count when dying
    private var votedGp: HashSet<GamePlayer> = hashSetOf()
    private val effectFlags = hashSetOf<Pair<Any, GamePlayerEffectFlag>>()
    private var countDownSecond: Int = 0
    private var countDownJob: Job? = null
    private var outlineInnocentJob: Job? = null
    private var glowingInnocentPlayers: List<GamePlayer> = emptyList()

    val hasArmor: Boolean
        get() = player.inventory.armorContents.any { it?.type == Material.IRON_CHESTPLATE }
    var stunLevel: Int = 0
        set(value) {
            if (!isInMainThread()) error("Async stunLevel change!")

            if (Game.state == GameState.END) {
                player.removePotionEffect(PotionEffectType.SLOW)
                player.removePotionEffect(PotionEffectType.JUMP)
                return
            }

            if (field >= 10 && value >= 10) {
                field = value
                return
            }
            field = value

            player.removePotionEffect(PotionEffectType.SLOW)
            player.removePotionEffect(PotionEffectType.JUMP)
            if (value > 0) {
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.JUMP,
                        10000000,
                        250,
                        false,
                        false,
                        false
                    )
                )
                player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SLOW,
                        10000000,
                        (value / 1.6).toInt(),
                        false,
                        false,
                        true
                    )
                )
            }
        }
    var bloodLevel: Int = 0
        set(value) {
            field = value.coerceAtMost(6)
            player.exp = field / 6f
        }
    val hasFuse: Boolean
        get() = gameItems.any { it is Fuse }
    val glowingEntityIDs: Set<Int>
        get() {
            val set = hashSetOf<Int>()
            if (gameItems.any { it is Antidote }) {
                set += gps.values
                    .filter { it != this && it.state == DYING && isInHighLightDistance(it.player) }
                    .map { it.player.entityId }
            }
            set += glowingInnocentPlayers.map { it.player.entityId }
            if ((Game.state == GameState.DARK || Game.state == GameState.RAGE) && !hasFuse) {
                set += player.world
                    .getEntitiesByClass(FallingBlock::class.java)
                    .filter { it.blockData.material == Config.fuseMaterial && isInHighLightDistance(it) }
                    .map { it.entityId }
            } else if ((Game.state == GameState.DARK || Game.state == GameState.RAGE) && hasFuse) {
                set += player.world
                    .getEntitiesByClass(FallingBlock::class.java)
                    .filter { it.blockData.material == Material.END_PORTAL_FRAME && isInHighLightDistance(it) }
                    .map { it.entityId }
            }
            return set
        }
    var lockGetItem = false
    var gameItems = Array<GameItem?>(9) { null }
    var state = NORMAL
        set(newValue) {
            if (!isInMainThread()) error("Async player state change!")
            if (newValue == field) return

            val plugin = Game.plugin
            if (field == TRANSFORMED && newValue == NORMAL) {
                countDownJob!!.cancel()
                countDownJob = null
                removeEffectFlag(GamePlayerEffectFlag.TRANSFORMED, this)
                GlobalScope.launch {
                    plugin.changeSkin(player, Config.originalSkinOverride[player.name] ?: player.name)
                    plugin.inMainThread {
                        player.inventory.heldItemSlot = 0
                    }
                }
            } else if (field == NORMAL && newValue == TRANSFORMED) {
                countDownSecond = Config.transformDuration
                countDownJob = GlobalScope.launch {
                    // transform
                    plugin.changeSkin(player, Config.infectedSkin)
                    plugin.inMainThread {
                        clearBloodLevel()

                        player.inventory.heldItemSlot = 8
                        addEffectFlag(GamePlayerEffectFlag.TRANSFORMED, this@GamePlayer)
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

                countDownSecond = Config.playerVotingDuration
                votedGp.clear()
                hologram = HologramsAPI.createHologram(
                    Game.plugin,
                    playerSitPos.clone().add(0.0, Config.votingTextHeight, 0.0)
                ).apply {
                    appendTextLine("Voting")
                    hologramVoteLine = appendTextLine(ChatColor.GRAY.toString() + "⬛".repeat(requiredVotes))
                }

                // sit
                player.health = 1.0
                sit(playerSitPos)

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

                unSit()

                player.health = Config.playerRespawnHealth
                player.teleport(Game.level.spawnPoses.random().toLocation())
            } else if (field == VOTING && newValue == DEAD) {
                countDownJob!!.cancel()
                countDownJob = null
                votedGp.clear()
                hologram!!.delete()
                hologram = null

                unSit()

                resetItemAndState()
                player.gameMode = GameMode.SPECTATOR
            } else if (field == DYING && newValue == DEAD) {
                countDownJob!!.cancel()
                countDownJob = null

                unSit()

                resetItemAndState()
                player.gameMode = GameMode.SPECTATOR
            } else if (field == DEAD && newValue == NORMAL) {
                player.gameMode = GameMode.ADVENTURE
            } else if (field == DYING && newValue == NORMAL) {
                countDownJob!!.cancel()
                countDownJob = null

                unSit()

                player.health = Config.playerRespawnHealth
            } else if (field == NORMAL && newValue == DYING) {
                val playerSitPos = player.getUnderBlockLocation()
                player.health = 1.0
                sit(playerSitPos)

                gps.values.forEach {
                    if (it != this) {
                        it.player.hidePlayer(Game.plugin, player)
                        it.player.showPlayer(Game.plugin, player)
                    }
                }

                countDownSecond = Config.playerDyingDuration
                countDownJob = GlobalScope.launch {
                    waitCountDown()
                    if (!isActive) return@launch

                    plugin.inMainThread {
                        state = DEAD
                    }
                }

            } else {
                plugin.log("Unknown player ${player.name} transfer: $field to $newValue")
            }
            field = newValue
        }

    init {
        scoreboardObjective =
            player.scoreboard.registerNewObjective(Random.nextLong().toString().take(16), "dummy", "MC Deciet")
        scoreboardObjective.displaySlot = DisplaySlot.SIDEBAR

        if (!debug) {
            player.gameMode = GameMode.ADVENTURE
            val spawn = Game.level.spawnPoses.random()
            player.teleport(player.location.apply { x = spawn.x; y = spawn.y; z = spawn.z })
        }
        resetItemAndState()
        addGameItem(TransformItem(isInfected))
        addGameItem(Crossbow())
        addGameItem(Arrow(4))
        addGameItem(LethalInjection())
        addGameItem(Tracker())
        addGameItem(Torch(64))
        addGameItem(Camera())

        player.sendTitle(
            if (isInfected) ChatColor.RED.toString() + "Infected" else "Innocent",
            "",
            10,
            60,
            10
        )

        Main.registerEvents(this)
        if (isInfected) {
            outlineInnocentJob = GlobalScope.launch {
                while (isActive) {
                    delay((Config.outlineInnocentDelay * 1000).toLong())

                    glowingInnocentPlayers = livingPlayers.filter { !it.isInfected && it.player.isSprinting }
                    Game.plugin.inMainThread {
                        glowingInnocentPlayers.forEach {
                            player.hidePlayer(Game.plugin, it.player)
                            player.showPlayer(Game.plugin, it.player)
                        }
                    }

                    delay((Config.outlineInnocentDuration * 1000).toLong())

                    val temp = glowingInnocentPlayers
                    glowingInnocentPlayers = emptyList()
                    Game.plugin.inMainThread {
                        temp.forEach {
                            player.hidePlayer(Game.plugin, it.player)
                            player.showPlayer(Game.plugin, it.player)
                        }
                    }
                }
            }
        }
        Game.addListener(GameEvent.ON_SECOND) {
            updateScoreBoard()
            if (canTransform) {
                player.inventory.contents[0]?.enchant()
            } else {
                player.inventory.contents[0]?.removeEnchant()
            }
        }
        Game.addListener(GameEvent.ON_END) {
            HandlerList.unregisterAll(this@GamePlayer)
            outlineInnocentJob?.cancel()
            resetItemAndState()
            updateScoreBoard()
            state = NORMAL
        }
    }

    private suspend fun CoroutineScope.waitCountDown() {
        while (countDownSecond > 0 && isActive) {
            delay(1000L)
            countDownSecond--
        }
        if (!isActive) {
            countDownSecond = 0
        }
    }

    private suspend fun JavaPlugin.changeSkin(player: Player, skinName: String) {
        inMainThread { consoleCommand("skin ${player.name} $skinName") }
        delay(300)
        inMainThread { consoleCommand("skinupdate ${player.name}") }
        delay(200)
    }

    private fun isInHighLightDistance(entity: Entity): Boolean {
        return entity.location.distanceSquared(player.location) < Config.highLightDistance * Config.highLightDistance
    }

    private fun sit(pos: Location) {
        rided = player.world.spawn(pos, Bat::class.java).apply {
            isInvulnerable = true
            setAI(false)
            addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 10000, 1, true, false))
            addPassenger(player)
        }
    }

    private fun unSit() {
        rided!!.removePassenger(player)
        rided!!.remove()
        rided = null
        player.teleport(player.location.clone().add(0.0, 1.0, 0.0))
    }

    private fun clearBloodLevel() {
        bloodLevel = 0
        player.exp = 0f
    }

    private fun resetItemAndState() {
        if (player.gameMode == GameMode.CREATIVE) return
        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
        player.level = 0
        player.exp = 0f
        player.foodLevel = 20
        player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        player.inventory.setArmorContents(arrayOf<ItemStack?>(null, null, null, null))
        player.inventory.setItemInOffHand(null)
        for (i in 0..8) {
            gameItems[i] = null
            player.inventory.setItem(i, null)
        }
    }

    private fun updateScoreBoard() {
        val obj = scoreboardObjective
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

        if (state == TRANSFORMED) {
            obj.getScore("Return to human in").score = countDownSecond
        } else if (state == VOTING) {
            obj.getScore("Respawn in").score = countDownSecond
        }
    }


    fun vote(gp: GamePlayer) {
        if (state != VOTING || gp in votedGp) return
        votedGp.add(gp)
        hologramVoteLine?.text = ChatColor.GREEN.toString() +
                "⬛".repeat(votedGp.size) +
                ChatColor.GRAY.toString() +
                "⬛".repeat(requiredVotes - votedGp.size)
        if (votedGp.size >= requiredVotes) {
            state = DEAD
        }
    }

    val canTransform: Boolean
        get() = isInfected && state == NORMAL && ((Game.state == GameState.DARK && bloodLevel == 6) || Game.state == GameState.RAGE)

    fun respawn() {
        player.health = if (state == TRANSFORMED) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        } else {
            Config.playerRespawnHealth
        }
        player.teleport(Game.level.spawnPoses.random().toLocation())
    }

    fun distanceSquared(other: GamePlayer) = player.location.distanceSquared(other.player.location)

    fun addGameItem(item: GameItem) {
        val index = gameItems.indexOfFirst { it == null }
        if (index != -1) {
            item.onAttach(this, index)
            if (item.getItemStack() != null) {
                gameItems[index] = item
            } else {
                item.onDetach()
            }
        }
    }

    fun removeGameItem(item: GameItem) {
        val index = gameItems.indexOfFirst { it == item }
        item.onDetach()
        if (index != -1) {
            gameItems[index] = null
        }
    }

    fun addEffectFlag(flag: GamePlayerEffectFlag, adder: Any) {
        if (!effectFlags.any { (_, oldFlag) -> oldFlag == flag }) {
            flag.applyTo(player)
        }
        effectFlags.add(adder to flag)
    }

    fun removeEffectFlag(flag: GamePlayerEffectFlag, adder: Any) {
        if (effectFlags.remove(adder to flag) &&
            !effectFlags.any { (_, oldFlag) -> oldFlag == flag }
        ) {
            flag.removeFrom(player)
        }
    }

    override fun toString() = "GamePlayer(${player.name}, state = ${state})"
}
