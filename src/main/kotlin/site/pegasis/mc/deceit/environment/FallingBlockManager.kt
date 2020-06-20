package site.pegasis.mc.deceit.environment

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.FallingBlock
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent
import site.pegasis.mc.deceit.inMainThread

data class ConsistentFallingBlock(
    val location: Location,
    val data: BlockData,
    var block: FallingBlock,
    var renewJob: Job? = null,
    var removed: Boolean = false
) {
    fun remove() {
        FallingBlockManager.remove(this)
    }
}

object FallingBlockManager {
    private val world = Bukkit.getWorld(Config.worldName)!!
    private val cfbs = arrayListOf<ConsistentFallingBlock>()
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        FallingBlockManager.plugin = plugin
    }

    fun hook() {
        Game.addListener(GameEvent.ON_END) {
            cfbs.forEach { cfb ->
                cfb.renewJob?.cancel()
                cfb.block.remove()
            }
            cfbs.clear()
        }
    }

    fun add(location: Location, data: BlockData): ConsistentFallingBlock {
        val fallingBlock =
            createFallingBlock(location, data)
        val cfb =
            ConsistentFallingBlock(location, data, fallingBlock)
        cfb.renewJob = createRenewJob(cfb)
        cfbs += cfb
        return cfb
    }

    fun remove(cfb: ConsistentFallingBlock) {
        if (cfb !in cfbs) return
        cfb.removed=true
        cfb.renewJob?.cancel()
        cfb.block.remove()
        cfbs.remove(cfb)
    }

    private fun createFallingBlock(location: Location, data: BlockData): FallingBlock {
        val fallingBlock = world.spawnFallingBlock(location, data)
        fallingBlock.setGravity(false)
        fallingBlock.dropItem = false
        return fallingBlock
    }

    fun createRenewJob(cfb: ConsistentFallingBlock): Job {
        return GlobalScope.launch {
            delay(1000)
            plugin.inMainThread {
                if (!cfb.removed){
                    cfb.block.remove()
                    cfb.block =
                        createFallingBlock(
                            cfb.location,
                            cfb.data
                        )
                    cfb.renewJob =
                        createRenewJob(cfb)
                }
            }
        }
    }
}
