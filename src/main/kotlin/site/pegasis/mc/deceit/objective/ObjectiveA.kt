package site.pegasis.mc.deceit.objective

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*
import kotlin.math.pow

class ObjectiveA(
    val pos: BlockPos,
    val leverPos: BlockPos,
    val gameItem: ItemStack,
    val plugin: JavaPlugin
) : Listener,
    Objective {
    val insidePlayers = arrayListOf<GamePlayer>()
    private var distroyed = false
    var progress = 0 // 0..12
    val changeProgressJob: Job
    var baseChangeInterval = Config.objADuration / 12
    val world = Bukkit.getWorld(Config.worldName)!!
    val lever = world.getBlockAt(leverPos)
    var activated = false
    val itemFrame: ItemFrame

    private fun getProgressBlockPos(p: Int): BlockPos {
        return with(pos) {
            when (p) {
                1 -> pos.copy(x = x + 2, z = z - 1)
                2 -> pos.copy(x = x + 2)
                3 -> pos.copy(x = x + 2, z = z + 1)

                4 -> pos.copy(z = z + 2, x = x + 1)
                5 -> pos.copy(z = z + 2, x = x)
                6 -> pos.copy(z = z + 2, x = x - 1)

                7 -> pos.copy(x = x - 2, z = z + 1)
                8 -> pos.copy(x = x - 2, z = z)
                9 -> pos.copy(x = x - 2, z = z - 1)

                10 -> pos.copy(z = z - 2, x = x - 1)
                11 -> pos.copy(z = z - 2, x = x)
                12 -> pos.copy(z = z - 2, x = x + 1)

                else -> error("Unknown progress: $p")
            }
        }

    }

    private fun resetBlocks(a: Boolean = activated) {
        for (i in 1..12) {
            val pos = getProgressBlockPos(i)
            world.getBlockAt(pos).setType(
                if (a)
                    Config.objANormalBlock
                else
                    Config.objAUnactivatedBlock
            )
        }
    }

    init {
        resetBlocks()
        powerOffLever()
        val itemFramePos = pos.toEntityPos()
            .copy(y = pos.y + 2.0, x = pos.x + 0.5, z = pos.z + 0.5)
            .toLocation(world)
        itemFrame = world.getNearbyEntities(itemFramePos, 0.3, 0.3, 0.3).first()!! as ItemFrame
        itemFrame.setItem(gameItem.clone())
        itemFrame.isInvulnerable = true

        changeProgressJob = GlobalScope.launch {
            while (progress < 12 && isActive) {
                if (insidePlayers.isEmpty() || !activated) {
                    plugin.inMainThread {
                        for (i in 1..progress) {
                            val pos = getProgressBlockPos(i)
                            world.getBlockAt(pos).setType(Config.objANormalBlock)
                        }
                        progress = 0
                    }
                    delay(1000)
                    continue
                }

                val changeInterval = baseChangeInterval * 0.8.pow(insidePlayers.size - 1)
                delay((changeInterval * 1000).toLong())
                progress++

                val blockPos = getProgressBlockPos(progress)
                plugin.inMainThread {
                    world.getBlockAt(blockPos).setType(Config.objAProgressBlock)
                }
            }
            if (progress == 12) {
                insidePlayers.firstOrNull()?.addGameItem(gameItem)
                itemFrame.setItem(null)
            }
        }
    }

    private fun powerOffLever() {
        lever.setBlockData((lever.blockData as Switch).apply { isPowered = false })
    }

    override fun destroy() {
        distroyed = true
        itemFrame.setItem(null)
        powerOffLever()
        changeProgressJob.cancel()
        resetBlocks(false)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!Game.started || distroyed || !activated) return
        val gp = event.player.getGP() ?: return
        val location = event.player.location
        if (location.x < pos.x + 0.5 + 2 &&
            location.x > pos.x + 0.5 - 2 &&
            location.z < pos.z + 0.5 + 2 &&
            location.z > pos.z + 0.5 - 2 &&
            location.y < pos.y + 4 &&
            location.y > pos.y
        ) {
            if (gp !in insidePlayers){
                insidePlayers += gp
            }
        } else {
            insidePlayers.remove(gp)
        }
    }

    @EventHandler
    fun onLeverPull(event: PlayerInteractEvent) {
        if (!Game.started || distroyed) return
        if (event.clickedBlock == lever) {
            val leverData = lever.blockData as Switch
            if (leverData.isPowered) {
                event.cancel()
            } else {
                activated = true
                resetBlocks()
            }
        }
    }
}
