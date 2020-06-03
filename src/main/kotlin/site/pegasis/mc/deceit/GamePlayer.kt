package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

data class GamePlayer(
    val player: Player,
    val isInfected: Boolean,
    var bloodLevel: Int = 0,
    var isDead: Boolean = false,
    val scoreboard: Scoreboard = createScoreBoard()
) {
    init {
        player.scoreboard = scoreboard
    }

    fun addBloodLevel(level: Int) {
        bloodLevel += level
        if (bloodLevel > 6) bloodLevel = 6
        player.exp = bloodLevel / 6f
    }

    fun clearBloodLevel() {
        bloodLevel = 0
        player.exp = 0f
    }


    companion object {
        val gps = arrayListOf<GamePlayer>()

        fun hook() {
            GameState.addListener(GameEvent.START) {
                val randomInfectedList = listOf(true, true, false, false, false, false).shuffled()
                Bukkit.getOnlinePlayers().take(6).forEachIndexed { i, player ->
                    player.level = 0
                    player.exp = 0f
                    player.foodLevel = 20
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
                    GameState.addListener(GameEvent.END) {
                        updateScoreBoard(gp)
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

            obj.getScore("Seconds left").score = GameState.secondToNextStage
        }
    }
}

fun Player.isBloodLevelFull(): Boolean {
    val found = GamePlayer.get(this)
    return found?.bloodLevel == 6
}

fun Player.isInfected(): Boolean {
    val found = GamePlayer.get(this)
    return found?.isInfected ?: false
}

fun Player.isGameDead(): Boolean {
    val found = GamePlayer.get(this)
    return found?.isDead ?: true
}
