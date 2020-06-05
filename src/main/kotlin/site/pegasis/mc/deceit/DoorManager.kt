package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.Material

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
                world.getBlockAt(pos).setType(Material.ANVIL)
            }
        }
    }
}
