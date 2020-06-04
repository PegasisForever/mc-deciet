package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.EndPortalFrame
import org.bukkit.entity.FallingBlock
import org.bukkit.plugin.java.JavaPlugin

data class FuseSocket(val block: Block, val fallingBlock: ConsistentFallingBlock) {
    var filled: Boolean = false
        set(value) {
            if (value) {
                val blockData = fallingBlock.block.blockData as EndPortalFrame
                blockData.setEye(true)

                fallingBlock.remove()
                FuseSocketManager.availableSockets.remove(this)

                FuseSocketManager.plugin.runDelayed(0.2){
                    block.setType(Material.END_PORTAL_FRAME)
                    block.setBlockData(blockData)
                }

                field = true
            }
        }
}

object FuseSocketManager {
    val availableSockets = arrayListOf<FuseSocket>()
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin){
        this.plugin=plugin
    }

    fun hook() {
        val world = Bukkit.getWorld(Config.worldName)!!
        GameState.addListener(GameEvent.START) {
            Config.fuseSocketPositions.forEach { pos ->
                val block = world.getBlockAt(pos)
                block.setType(Material.END_PORTAL_FRAME)
            }
        }
        GameState.addListener(GameEvent.DARK) {
            Config.fuseSocketPositions.forEach { pos ->
                val block = world.getBlockAt(pos)
                val originalBlockData = block.blockData
                block.setType(Material.AIR)
                val fallingBlock = FallingBlockManager.add(
                    block.location.clone().apply { x += 0.5;z += 0.5 },
                    originalBlockData
                )
                availableSockets += FuseSocket(block, fallingBlock)
            }
        }
        GameState.addListener(GameEvent.LIGHT) {
            availableSockets.forEach { socket ->
                socket.fallingBlock.remove()
            }
            availableSockets.clear()
            runDelayed(0.2){
                Config.fuseSocketPositions.forEach { pos ->
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
