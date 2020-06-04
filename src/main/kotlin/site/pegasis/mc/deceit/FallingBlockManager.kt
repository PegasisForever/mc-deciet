package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.FallingBlock
import org.bukkit.plugin.java.JavaPlugin

data class ConsistentFallingBlock(
    val location: Location,
    val data: BlockData,
    var block: FallingBlock,
    var renewJob: Job? = null
)

object FallingBlockManager {
    private val world = Bukkit.getWorld(Config.worldName)!!
    private val cfbs = arrayListOf<ConsistentFallingBlock>()
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun hook() {
        GameState.addListener(GameEvent.END) {
            cfbs.forEach { cfb->
                cfb.renewJob?.cancel()
                cfb.block.remove()
            }
            cfbs.clear()
        }
    }

    fun add(location: Location, data: BlockData): ConsistentFallingBlock {
        val fallingBlock = createFallingBlock(location, data)
        val cfb = ConsistentFallingBlock(location, data, fallingBlock)
        cfb.renewJob = createRenewJob(cfb)
        cfbs += cfb
        return cfb
    }

    fun remove(cfb: ConsistentFallingBlock) {
        if (cfb !in cfbs) return
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
            delay(20_000)
            plugin.inMainThread {
                cfb.block.remove()
                cfb.block = createFallingBlock(cfb.location, cfb.data)
                cfb.renewJob = createRenewJob(cfb)
            }
        }
    }
}
