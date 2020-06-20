package site.pegasis.mc.deceit.objective.fuse

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.environment.FallingBlockManager

object FuseManager {
    val availableFuses = arrayListOf<FuseEntityBlock>()
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        FuseManager.plugin = plugin
    }

    fun hook() {
        val world = Bukkit.getWorld(Config.worldName)!!
        Game.addListener(GameEvent.ON_LEVEL_START) {
            Game.level.fusePositions.forEach { pos ->
                world.getBlockAt(pos).setType(Material.AIR)
                world.getBlockAt(pos.copy(y = pos.y - 1)).setType(Material.AIR)
            }
        }
        Game.addListener(GameEvent.ON_DARK) {
            Game.level.fusePositions.shuffled()
                .take(Game.level.fuseCount)
                .forEach { pos ->
                    world.getBlockAt(pos.copy(y = pos.y - 1))
                        .setType(Config.fuseBaseMaterial)

                    val block = world.getBlockAt(pos)
                    val fallingBlock = FallingBlockManager.add(
                        block.location.clone().apply { x += 0.5; z += 0.5 },
                        Config.fuseMaterial.createBlockData()
                    )
                    // todo add id to glowing list
                    availableFuses += FuseEntityBlock(
                        block,
                        fallingBlock
                    )
                }
        }
        Game.addListener(GameEvent.ON_LEVEL_END) {
            availableFuses.forEach { fuse ->
                fuse.destroy()
            }
            availableFuses.clear()

            val positions = Game.level.fusePositions
            plugin.runDelayed(Config.removeEntityWaitSecond) {
                positions.forEach { pos ->
                    world.getBlockAt(pos).setType(Config.fuseMaterial)
                    world.getBlockAt(pos.copy(y = pos.y - 1))
                        .setType(Config.fuseBaseMaterial)
                }
            }
        }
    }
}
