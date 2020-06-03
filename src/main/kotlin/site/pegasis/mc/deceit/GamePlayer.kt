package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.entity.Player

data class GamePlayer(
    val player: Player,
    val isInfected: Boolean,
    var bloodLevel: Int = 0,
    var isDead: Boolean = false
) {
    fun addBloodLevel(level: Int) {
        bloodLevel += level
        if (bloodLevel > 6) bloodLevel = 6
        player.exp = bloodLevel / 6f
    }

    companion object {
        val list = arrayListOf<GamePlayer>()

        fun start() {
            list.clear()
            val randomInfectedList = listOf(true, true, false, false, false, false).shuffled()
            Bukkit.getOnlinePlayers().take(6).forEachIndexed { i, player ->
                player.level = 0
                player.exp = 0f
                player.foodLevel = 20
                if (debug){
                    list.add(GamePlayer(player, true))
                }else{
                    list.add(GamePlayer(player, randomInfectedList[i]))
                }

            }
        }

        fun get(player: Player) = list.find { it.player == player }
    }
}

fun Player.isBloodLevelFull():Boolean{
    val found = GamePlayer.get(this)
    return found?.bloodLevel==6
}

fun Player.isInfected(): Boolean {
    val found = GamePlayer.get(this)
    return found?.isInfected ?: false
}

fun Player.isGameDead(): Boolean {
    val found = GamePlayer.get(this)
    return found?.isDead ?: true
}
