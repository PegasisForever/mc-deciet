package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import kotlin.random.Random

object Environment {
    fun hook() {
        GameState.addListener(GameEvent.START) {
            consoleCommand("time set midnight")
        }
        GameState.addListener(GameEvent.LIGHT){
            lightOn()
        }
        GameState.addListener(GameEvent.DARK){
            lightOff()
        }
    }

    private val torchBlocks = arrayListOf<Block>()
    private val potBlocks = arrayListOf<Pair<Block, Material>>()
    private var lightOn = true

    fun lightOn() {
        if (lightOn) return
        lightOn = true

        torchBlocks.forEach { block ->
            block.setType(Material.TORCH)
        }
        potBlocks.forEach { (block, originalType) ->
            block.setType(originalType)
        }
    }

    fun lightOff() {
        if (!lightOn) return
        lightOn = false

        torchBlocks.clear()
        val world = Bukkit.getWorld(Config.worldName)!!
        world.loadedChunks.forEach { chunk ->
            chunk.forEachBlock { block ->
                if (block.type == Material.TORCH) {
                    if (Random.nextInt(10) <= 6) {
                        block.setType(Material.REDSTONE_TORCH)
                    } else {
                        block.setType(Material.AIR)
                    }
                    torchBlocks.add(block)
                } else if (block.type.toString().startsWith("POTTED")) {
                    potBlocks += (block to block.type)
                    if (Random.nextBoolean()) {
                        block.setType(Material.POTTED_DEAD_BUSH)
                    } else {
                        block.setType(Material.POTTED_WITHER_ROSE)
                    }
                }
            }
        }
    }
}
