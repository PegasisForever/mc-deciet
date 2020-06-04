package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.plugin.java.JavaPlugin

data class Fuse(val block: Block, val fallingBlock: ConsistentFallingBlock) {
    var taken: Boolean = false
        set(value) {
            if (value) {
                fallingBlock.remove()
                FuseManager.availableFuses.remove(this)
                field = value
            }
        }
}

object FuseManager {
    val availableFuses = arrayListOf<Fuse>()
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin){
        this.plugin=plugin
    }

    fun hook() {
        val world = Bukkit.getWorld(Config.worldName)!!
        GameState.addListener(GameEvent.START) {
            Config.fusePositions.forEach { pos ->
                val block = world.getBlockAt(pos)
                block.setType(Config.fuseMaterial)
            }
        }
        GameState.addListener(GameEvent.DARK) {
            Config.fusePositions.forEach { pos ->
                val block = world.getBlockAt(pos)
                val originalBlockData = block.blockData
                block.setType(Material.AIR)
                val fallingBlock = FallingBlockManager.add(
                    block.location.clone().apply { x += 0.5;z += 0.5 },
                    originalBlockData
                )
                availableFuses += Fuse(block, fallingBlock)
            }
        }
        GameState.addListener(GameEvent.LIGHT) {
            availableFuses.forEach { fuse->
                fuse.fallingBlock.remove()
            }
            availableFuses.clear()
            plugin.runDelayed(0.2){
                Config.fusePositions.forEach { pos ->
                    val block = world.getBlockAt(pos)
                    block.setType(Config.fuseMaterial)
                }
            }
        }
    }

    fun getFuse(block: FallingBlock): Fuse? {
        return availableFuses.find { it.fallingBlock.block == block }
    }
}
