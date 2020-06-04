package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock

data class Fuse(val block: Block, val fallingBlock: ConsistentFallingBlock) {
    var taken: Boolean = false
        set(value) {
            if (value) {
                fallingBlock.remove()
                field = value
            }
        }
}

object FuseManager {
    private val availableFuses = arrayListOf<Fuse>()

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
                fuse.block.setType(Config.fuseMaterial)
            }
            availableFuses.clear()
        }
    }

    fun getFuse(block: FallingBlock): Fuse? {
        return availableFuses.find { it.fallingBlock.block == block }
    }
}
