package site.pegasis.mc.deceit.environment

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Lantern
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.BannerState.Companion.applyState
import kotlin.random.Random

object LightManager {
    fun hook() {
        Game.addListener(GameEvent.ON_LIGHT_ON) {
            lightOn()
        }
        Game.addListener(GameEvent.ON_LIGHT_OFF) {
            lightOff()
        }
    }

    private val torchBlocks = arrayListOf<Block>()
    private val potBlocks = arrayListOf<Pair<Block, Material>>()
    private val lanternBlocks = arrayListOf<Pair<Block, Boolean>>()
    private val pumpkinBlocks = arrayListOf<Pair<Block, BlockFace>>()
    private val bannerBlocks = arrayListOf<Pair<Block, BannerState>>()
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
        bannerBlocks.forEach { (block, oldBannerState) ->
            val banner = block.state as Banner
            banner.applyState(oldBannerState)
            banner.update()
        }
    }

    // fixme some torch wont off
    fun lightOff(full: Boolean = false) {
        if (!lightOn) return
        lightOn = false

        torchBlocks.clear()
        potBlocks.clear()
        lanternBlocks.clear()
        pumpkinBlocks.clear()

        val replaceAction: (Block) -> Unit = { block: Block ->
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
            } else if (block.type.toString().endsWith("BANNER")) {
                val banner = block.state as Banner
                for ((from, to) in Config.bannerStates) {
                    if (from.isMatch(banner)) {
                        bannerBlocks += (block to from)
                        banner.applyState(to)
                        banner.update()
                        break
                    }
                }
            }
        }

        val world = Bukkit.getWorld(Config.worldName)!!
        if (full) {
            world.loadedChunks.forEach { chunk ->
                chunk.forEachBlock(replaceAction)
            }
        } else {
            Config.lightSources.forEach { pos ->
                replaceAction(world.getBlockAt(pos))
            }
        }

    }
}
