package site.pegasis.mc.deceit

import org.bukkit.Bukkit

object FuseManager {
    fun hook() {
        GameState.addListener(GameEvent.START) {
            val world = Bukkit.getWorld(Config.worldName)!!
            world.loadedChunks.forEach { chunk ->
                chunk.forEachBlock { block ->
                    if (block.type == Config.fuseMaterial) {
                        FallingBlockManager.add(block.location.clone().apply { y += 0.01;x += 1 }, block.blockData)
                    }
                }
            }
        }
    }
}
