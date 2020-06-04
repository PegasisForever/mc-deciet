package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Lantern
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

object Environment {
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun hook() {
        GameState.addListener(GameEvent.START) {
            consoleCommand("time set midnight")
        }
        GameState.addListener(GameEvent.LIGHT) {
            lightOn()
        }
        GameState.addListener(GameEvent.DARK) {
            lightOff()
        }
    }

    private val torchBlocks = arrayListOf<Block>()
    private val potBlocks = arrayListOf<Pair<Block, Material>>()
    private val lanternBlocks = arrayListOf<Pair<Block, Boolean>>()
    private val pumpkinBlocks = arrayListOf<Pair<Block, BlockFace>>()
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
        lanternBlocks.forEach { (block, isHanging) ->
            block.setType(Material.LANTERN)
            block.setBlockData((block.blockData as Lantern).apply { setHanging(isHanging) })
        }
        pumpkinBlocks.forEach { (block, facing) ->
            block.setType(Material.JACK_O_LANTERN)
            block.setBlockData((block.blockData as Directional).apply { setFacing(facing) })
        }
    }

    fun lightOff() {
        if (!lightOn) return
        lightOn = false

        torchBlocks.clear()
        potBlocks.clear()
        lanternBlocks.clear()
        pumpkinBlocks.clear()
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
                } else if (block.type == Material.LANTERN) {
                    lanternBlocks += (block to (block.blockData as Lantern).isHanging)
                    block.setType(Material.AIR)
                } else if (block.type == Material.JACK_O_LANTERN) {
                    val facing = (block.blockData as Directional).facing
                    pumpkinBlocks += (block to facing)
                    block.setType(Material.CARVED_PUMPKIN)
                    block.setBlockData((block.blockData as Directional).apply { setFacing(facing) })
                }
            }
        }
    }
}
