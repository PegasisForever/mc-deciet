package site.pegasis.mc.deceit.objective.fuse

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.environment.FallingBlockManager

object FuseSocketManager {
    val availableSockets = arrayListOf<FuseSocket>()
    var filledSockets = 0
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        FuseSocketManager.plugin = plugin
    }

    fun hook() {
        val world = Bukkit.getWorld(Config.worldName)!!
        Game.addListener(GameEvent.ON_LEVEL_START) {
            Game.level.fuseSocketPositions.forEach { pos ->
                world.getBlockAt(pos).setType(Material.AIR)
            }
        }
        Game.addListener(GameEvent.ON_DARK) {
            Game.level.fuseSocketPositions.shuffled()
                .take(Game.level.fuseSocketCount)
                .forEach { pos ->
                    val block = world.getBlockAt(pos)
                    val fallingBlock = FallingBlockManager.add(
                        block.location.clone().apply { x += 0.5; z += 0.5 },
                        Material.END_PORTAL_FRAME.createBlockData()
                    )
                    availableSockets += FuseSocket(
                        block,
                        fallingBlock
                    )
                }
        }
        Game.addListener(GameEvent.ON_LEVEL_END) {
            filledSockets = 0

            availableSockets.forEach { socket ->
                socket.fallingBlock.remove()
            }
            availableSockets.clear()

            val positions = Game.level.fuseSocketPositions
            runDelayed(Config.removeEntityWaitSecond) {
                positions.forEach { pos ->
                    val block = world.getBlockAt(pos)
                    block.setType(Material.END_PORTAL_FRAME)
                }
            }
        }
    }

    fun getSocket(block: FallingBlock): FuseSocket? {
        return availableSockets.find { it.fallingBlock.block == block }
    }
}
