package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Transform(val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onLeftClick(event: PlayerInteractEvent) {
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            val itemInHand = event.player.inventory.itemInMainHand
            val player = event.player
            if (itemInHand.type == Material.ENDER_EYE && GameState.dark && player.isInfected() && player.isBloodLevelFull()) {
                //transform
                GlobalScope.launch {
                    GamePlayer.get(player)!!.bloodLevel = 0
                    player.exp = 0f

                    plugin.inMainThread { plugin.consoleCommand("skin ${player.name} ${Config.infectedSkin}") }
                    delay(300)
                    plugin.inMainThread { plugin.consoleCommand("skinupdate ${player.name}") }
                    delay(200)
                    plugin.inMainThread {
                        player.inventory.heldItemSlot = 8
                        player.addPotionEffect(
                            PotionEffect(
                                PotionEffectType.NIGHT_VISION,
                                10000,
                                1
                            )
                        )
                        player.addPotionEffect(
                            PotionEffect(
                                PotionEffectType.SPEED,
                                10000,
                                1
                            )
                        )
                    }
                    delay(Config.transformDuration * 1000L)

                    plugin.inMainThread {
                        player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
                    }
                    delay(100)
                    plugin.inMainThread { plugin.consoleCommand("skin ${player.name} ${Config.originalSkinOverride[player.name] ?: player.name}") }
                    delay(300)
                    plugin.inMainThread { plugin.consoleCommand("skinupdate ${player.name}") }
                }
            }
        }
    }
}
