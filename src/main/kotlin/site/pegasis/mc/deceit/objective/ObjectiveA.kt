package site.pegasis.mc.deceit.objective

import kotlinx.coroutines.*
import org.bukkit.Location
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import kotlin.math.pow
import site.pegasis.mc.deceit.objective.ObjectiveA.State.*

class ObjectiveA(
    private val pos: BlockPos,
    leverPos: BlockPos,
    private val gameItem: ItemStack
) : Objective {
    private enum class State {
        INACTIVATED,
        WAITING,
        PROGRESSING,
        COMPLETED,
        DESTROYED
    }

    private val lever = Game.world.getBlockAt(leverPos)
    private val itemFrame: ItemFrame

    private var changeProgressJob: Job? = null

    private val baseChangeInterval = Config.objADuration / 12
    private val insidePlayers = arrayListOf<GamePlayer>()
    private var progress = 0 // 0..12

    private fun complete() {
        changeProgressJob?.cancel()
        changeProgressJob = null
        itemFrame.setItem(null)
    }

    private fun destroy() {
        HandlerList.unregisterAll(this)
        changeProgressJob?.cancel()
        changeProgressJob = null
        progress = 0
        resetBlocks()
    }

    private var state = INACTIVATED
        set(value) {
            if (value == field) return
            if (value == DESTROYED) {
                complete()
                destroy()
            } else if (field == INACTIVATED && value == WAITING) {
                setBlocksWaiting()
                itemFrame.setItem(gameItem.clone())
            } else if (field == WAITING && value == PROGRESSING) {
                changeProgressJob = GlobalScope.launch {
                    while (isActive) {
                        val changeInterval = baseChangeInterval * 0.8.pow(insidePlayers.size - 1)
                        delay((changeInterval * 1000).toLong())
                        if (!isActive) break
                        progress++

                        val blockPos = getProgressBlockPos(progress)
                        Game.plugin.inMainThread {
                            Game.world.getBlockAt(blockPos).setType(Config.objAProgressBlock)
                        }

                        if (progress == 12) {
                            insidePlayers.first().addGameItem(gameItem)
                            state = COMPLETED
                        }
                    }
                }
            } else if (field == PROGRESSING && value == WAITING) {
                changeProgressJob!!.cancel()
                changeProgressJob = null
                setBlocksWaiting()
                progress = 0
            } else if (field == PROGRESSING && value == COMPLETED) {
                complete()
            }
            field = value
        }


    init {
        resetBlocks()
        val itemFramePos = pos.toEntityPos()
            .copy(y = pos.y + 2.0, x = pos.x + 0.5, z = pos.z + 0.5)
            .toLocation()
        itemFrame = Game.world.getNearbyEntities(itemFramePos, 0.3, 0.3, 0.3).first()!! as ItemFrame
        itemFrame.isInvulnerable = true
    }

    override fun destroyAndReset() {
        state = DESTROYED
    }

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

    private fun resetBlocks() {
        for (i in 1..12) {
            val pos = getProgressBlockPos(i)
            Game.world.getBlockAt(pos).setType(Config.objAUnactivatedBlock)
        }
        lever.setBlockData((lever.blockData as Switch).apply { isPowered = false })
    }

    private fun setBlocksWaiting() {
        for (i in 1..12) {
            val pos = getProgressBlockPos(i)
            Game.world.getBlockAt(pos).setType(Config.objANormalBlock)
        }
    }

    private fun Location.isInArea() = x < pos.x + 0.5 + 2 &&
            x > pos.x + 0.5 - 2 &&
            z < pos.z + 0.5 + 2 &&
            z > pos.z + 0.5 - 2 &&
            y < pos.y + 4 &&
            y > pos.y

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (state != WAITING && state != PROGRESSING) return
        val gp = event.player.getGP() ?: return
        if (gp.state == PlayerState.DEAD) return
        if (event.player.location.isInArea()) {
            if (gp !in insidePlayers) {
                insidePlayers += gp
                state = PROGRESSING
            }
        } else {
            if (insidePlayers.remove(gp)) {
                state = if (insidePlayers.isEmpty()) WAITING else PROGRESSING
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onLeverPull(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock == lever) {
            if (state == INACTIVATED) {
                state = WAITING
            } else {
                event.cancel()
            }
        }
    }
}
