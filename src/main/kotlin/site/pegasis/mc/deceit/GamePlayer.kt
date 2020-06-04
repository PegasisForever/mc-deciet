package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

data class GamePlayer(
    val player: Player,
    val isInfected: Boolean,
    var isDead: Boolean = false,
    var transformed: Boolean = false,
    var secondToHuman: Int = 0,
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
                player.inventory.setItem(2, ItemStack(Config.fuseMaterial))
            } else {
                player.inventory.contents.forEach { itemStack: ItemStack? ->
                    if (itemStack?.type == Config.fuseMaterial) {
                        player.inventory.remove(itemStack)
                    }
                }
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
                    .map { it.entityId }
            } else if ((Game.state == GameState.DARK || Game.state == GameState.RAGE) && hasFuse) {
                set += player.world
                    .getEntitiesByClass(FallingBlock::class.java)
                    .filter { it.blockData.material == Material.END_PORTAL_FRAME }
                    .map { it.entityId }
            }
            return set
        }


    init {
        player.scoreboard = scoreboard
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

    private suspend fun startTransform(plugin: JavaPlugin) {
        if (transformed) return
        clearBloodLevel()
        transformed = true
        secondToHuman = Config.transformDuration

        plugin.changeSkin(player, Config.infectedSkin)
        plugin.inMainThread {
            player.inventory.heldItemSlot = 8
            player.addInfectedEffect()
        }
    }

    private suspend fun endTransform(plugin: JavaPlugin) {
        if (!transformed) return
        transformed = false
        secondToHuman = 0

        plugin.inMainThread { player.removeAllEffect() }
        plugin.changeSkin(player, Config.originalSkinOverride[player.name] ?: player.name)
    }

    suspend fun transform(plugin: JavaPlugin) {
        startTransform(plugin)

        repeat(Config.transformDuration) {
            delay(1000L)
            secondToHuman--
        }

        endTransform(plugin)
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
                    player.level = 0
                    player.exp = 0f
                    player.foodLevel = 20
                    player.inventory.apply {
                        setItem(0, ItemStack(Config.transformMaterial))
                        setItem(1, ItemStack(Material.COMPASS))
                        contents[2]?.let { remove(it) }
                    }
                    val gp = if (debug) {
                        GamePlayer(player, true)
                    } else {
                        GamePlayer(player, randomInfectedList[i])
                    }
                    gps += gp
                    if (!debug) {
                        val spawn = Config.spawnPoses.random()
                        player.teleport(player.location.apply { x = spawn.x; y = spawn.y; z = spawn.z })
                    }
                    player.sendTitle(if (gp.isInfected) "Infected" else "Innocent", "", 10, 60, 10)

                    Game.addListener(GameEvent.ON_SECOND) {
                        updateScoreBoard(gp)
                        if (gp.canTransform()) {
                            player.inventory.contents[0].enchant()
                        } else {
                            player.inventory.contents[0].removeEnchant()
                        }
                    }
                    Game.addListener(GameEvent.ON_END) inner@{
                        GlobalScope.launch {
                            gp.endTransform(this@inner)
                            this@inner.inMainThread {
                                updateScoreBoard(gp)
                            }
                        }
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
            "Next Black out",
            "Enrage in",
            "Time remaining",
            "Go to next area in",
            "Game ended",
            "Return to human in"
        )

        fun updateScoreBoard(gp: GamePlayer) {
            val obj = gp.scoreboard.objectives.first()
            obj.displayName = when (Game.state) {
                GameState.LIGHT -> "Light On"
                GameState.DARK -> "Black out"
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
                GameState.END -> texts[4]
            }
            obj.getScore(text).score = Game.secondToNextStage

            if (gp.transformed) {
                obj.getScore(texts[5]).score = gp.secondToHuman
            }
        }
    }
}

fun Player.getGP() = GamePlayer.get(this)
