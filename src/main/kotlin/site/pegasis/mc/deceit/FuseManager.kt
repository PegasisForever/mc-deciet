package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block

data class Fuse(val block: Block, val fallingBlock: ConsistentFallingBlock) {
    var taken: Boolean = false
        set(value) {
            if (value) {
                fallingBlock.remove()
                block.type = Material.AIR
                field = value
            }
        }
}

object FuseManager {
    val fuses = arrayListOf<Fuse>()

    fun hook() {
        GameState.addListener(GameEvent.START) { // DARK
            val world = Bukkit.getWorld(Config.worldName)!!
            world.loadedChunks.forEach { chunk ->
                chunk.forEachBlock { block ->
                    if (block.type == Config.fuseMaterial) {
                        val glassBlockData = block.blockData
//                        block.setType(Material.AIR)
                        //fixme falling block replace original block
                        //todo use onInteractEntity
                        val fallingBlock =
                            FallingBlockManager.add(
                                block.location.clone().apply { x += 0.5;z += 0.5 },
                                glassBlockData
                            )
                        fuses += Fuse(block, fallingBlock)
                    }
                }
            }
        }
    }

    fun getFuse(block: Block): Fuse? {
        return fuses.find { it.block == block }
    }
}