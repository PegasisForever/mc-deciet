package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Transform(val plugin: JavaPlugin) : Listener {
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

    @EventHandler
    fun onLeftClick(event: PlayerInteractEvent) {
        val itemInHand = event.player.inventory.itemInMainHand
        val player = event.player
        if (event.action == Action.LEFT_CLICK_AIR &&
            itemInHand.type == Material.ENDER_EYE &&
            GameState.dark &&
            GameState.started &&
            player.isInfected() &&
            player.isBloodLevelFull()
        ) {
            GlobalScope.launch {
                GamePlayer.get(player)!!.clearBloodLevel()

                plugin.changeSkin(player, Config.infectedSkin)
                plugin.inMainThread {
                    player.inventory.heldItemSlot = 8
                    player.addInfectedEffect()
                }

                delay(Config.transformDuration * 1000L)

                plugin.inMainThread {
                    player.removeAllEffect()
                }
                plugin.changeSkin(player, Config.originalSkinOverride[player.name] ?: player.name)
            }

        }
    }
}
