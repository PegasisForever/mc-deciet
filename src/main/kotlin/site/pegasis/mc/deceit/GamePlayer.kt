package site.pegasis.mc.deceit

import com.gmail.filoghost.holographicdisplays.api.Hologram
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import com.gmail.filoghost.holographicdisplays.api.line.TextLine
import kotlinx.coroutines.*
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import kotlin.random.Random
import site.pegasis.mc.deceit.PlayerState.*
import site.pegasis.mc.deceit.gameitem.*
import site.pegasis.mc.deceit.gameitem.Arrow
import site.pegasis.mc.deceit.gameitem.Fuse

enum class PlayerState {
    NORMAL,
    TRANSFORMED,
    VOTING,
    DYING,
    DEAD,
    REMOVED
}

data class GamePlayer(
    val player: Player,
    val isInfected: Boolean
) : Listener {
    var torchLightBlock: Block? = null
    var countDownSecond: Int = 0
    var countDownJob: Job? = null
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
    var gameItems = Array<GameItem?>(9) { null }
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

                countDownSecond = Config.playerVotingDuration
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
        objective = player.scoreboard.registerNewObjective(Random.nextLong().toString().take(16), "dummy", "MC Deciet")
        objective.displaySlot = DisplaySlot.SIDEBAR
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

    private fun inHighLightDistance(entity: Entity): Boolean {
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

    fun canTransform() =
        isInfected && state == NORMAL && ((Game.state == GameState.DARK && bloodLevel == 6) || Game.state == GameState.RAGE)

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
                PotionEffectType.SPEED,
                10000000,
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

    fun addGameItem(item: GameItem) {
        val index = gameItems.indexOfFirst { it == null }
        if (index != -1) {
            item.onAttach(this, index)
            gameItems[index] = item
        }
    }

    fun removeGameItem(item: GameItem) {
        val index = gameItems.indexOfFirst { it == item }
        item.onDetach()
        if (index != -1) {
            gameItems[index] = null
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

        if (state == TRANSFORMED) {
            obj.getScore("Return to human in").score = countDownSecond
        } else if (state == VOTING) {
            obj.getScore("Respawn in").score = countDownSecond
        }
    }

    fun holdingItem(slot: Int? = null): ItemStack? {
        return if (slot != null) {
            player.inventory.getItem(slot)
        } else {
            player.inventory.itemInMainHand
        }
    }

    companion object {
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
                    gp.addGameItem(Camera(6))
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
                        gp.state = NORMAL
                        gp.resetItemAndState()
                    }
                }
            }

            Game.addListener(GameEvent.ON_END) {
                gps.clear()
            }
        }

        fun livingPlayers() = gps.values.filter { it.state != DEAD }

        fun getRequiredVotes() = (livingPlayers().size - 1) / 2 + 1
    }
}

fun Player.getGP() = GamePlayer.gps[player]
