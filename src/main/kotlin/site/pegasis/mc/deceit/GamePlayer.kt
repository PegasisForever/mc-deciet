package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
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
            if (!GameState.started) return emptySet()

            val set = hashSetOf<Int>()
            if (GameState.dark && hasFuse) {
                set += player.world
                    .getEntitiesByClass(FallingBlock::class.java)
                    .filter { it.blockData.material == Config.fuseMaterial }
                    .map { it.entityId }
            } else if (GameState.dark && !hasFuse) {

            }
            return set
        }


    init {
        player.scoreboard = scoreboard
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
            GameState.addListener(GameEvent.START) {
                val randomInfectedList = listOf(true, true, false, false, false, false).shuffled()
                Bukkit.getOnlinePlayers().take(6).forEachIndexed { i, player ->
                    player.level = 0
                    player.exp = 0f
                    player.foodLevel = 20
                    player.inventory.apply {
                        setItem(0, ItemStack(Config.transformMaterial))
                        setItem(1, ItemStack(Material.COMPASS))
                    }
                    val gp = if (debug) {
                        GamePlayer(player, true)
                    } else {
                        GamePlayer(player, randomInfectedList[i])
                    }
                    gps += gp
                    gp.player.sendTitle(if (gp.isInfected) "Infected" else "Innocent", "", 10, 60, 10)

                    GameState.addListener(GameEvent.SECOND) {
                        updateScoreBoard(gp)
                    }
                    GameState.addListener(GameEvent.END) inner@{
                        GlobalScope.launch {
                            gp.endTransform(this@inner)
                            this@inner.inMainThread {
                                updateScoreBoard(gp)
                            }
                        }
                    }
                }
            }

            GameState.addListener(GameEvent.END) {
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

        fun updateScoreBoard(gp: GamePlayer) {
            val obj = gp.scoreboard.objectives.first()
            obj.displayName = when {
                !GameState.started -> "Game End"
                GameState.dark -> "Dark"
                else -> "Light"
            }

            obj.getScore("Seconds left ").score = GameState.secondToNextStage
            if (gp.transformed) {
                obj.getScore("Return to human ").score = gp.secondToHuman
            } else {
                obj.scoreboard?.resetScores("Return to human ")
            }
        }
    }
}

fun Player.getGP() = GamePlayer.get(this)
