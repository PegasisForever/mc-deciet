package site.pegasis.mc.deceit.environment

import org.bukkit.Bukkit
import org.bukkit.Material
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent
import site.pegasis.mc.deceit.getBlockAt

object DoorManager {
    fun hook() {

        Game.addListener(GameEvent.ON_RUN) {
            val world = Bukkit.getWorld(Config.worldName)!!
            Game.level.doorPositions.forEach { pos ->
                world.getBlockAt(pos).setType(Material.AIR)
            }
        }
        Game.addListener(GameEvent.ON_LEVEL_END) {
            val world = Bukkit.getWorld(Config.worldName)!!
            Game.level.doorPositions.forEach { pos ->
                world.getBlockAt(pos).setType(Config.doorMaterial)
            }
        }
    }
}
