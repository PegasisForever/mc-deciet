package site.pegasis.mc.deceit.objective

import kotlinx.coroutines.*
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.Arrow
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.gameitem.GameItem
import site.pegasis.mc.deceit.objective.ObjectiveB.State.*

class ObjectiveB(
    val pos: BlockPos,
    leverPos: BlockPos,
    itemFrameBlockPos: BlockPos,
    private val gameItem: GameItem
) : Objective {
    private enum class State {
        INACTIVATED,
        ACTIVATED,
        COMPLETED,
        DESTROYED
    }

    private val lever = Game.world.getBlockAt(leverPos)
    private val itemFrame: ItemFrame
    private var button: Block? = null
    private lateinit var buttonSide: BlockFace
    private var rotateJob: Job? = null

    private var level = 0

    // state setter use only
    private fun active() {
        itemFrame.setItem(gameItem.getItemStack()!!.clone())
        button = Game.world.getBlockAt(pos.copy(x = pos.x + 1))
        button!!.setType(Config.objBButtonMaterial)
        setButtonFacing(BlockFace.EAST)
        rotateJob = GlobalScope.launch {
            while (isActive) {
                val rotateInterval = getRotateInterval()
                delay((rotateInterval * 1000).toLong())
                if (!isActive) break
                Game.plugin.inMainThread {
                    rotateButton()
                }
            }
        }
    }

    // state setter use only
    private fun complete() {
        itemFrame.setItem(null)
        rotateJob?.cancel()
        rotateJob = null
    }

    // state setter use only
    private fun destroy() {
        HandlerList.unregisterAll(this)
        resetBlocks()
    }

    private var state = INACTIVATED
        set(value) {
            if (value == field) return
            if (value == DESTROYED) {
                complete()
                destroy()
            } else if (field == INACTIVATED && value == ACTIVATED) {
                active()
            } else if (field == ACTIVATED && value == COMPLETED) {
                complete()
            } else {
                error("Unknown state change: $field to $value")
            }
            field = value
        }

    init {
        resetBlocks()
        val itemFramePos = itemFrameBlockPos.toEntityPos()
            .copy(y = itemFrameBlockPos.y + 1.0, x = itemFrameBlockPos.x + 0.5, z = itemFrameBlockPos.z + 0.5)
            .toLocation()
        itemFrame = Game.world.getNearbyEntities(itemFramePos, 0.3, 0.3, 0.3).first() as ItemFrame
        itemFrame.isInvulnerable = true
    }

    override fun destroyAndReset() {
        state = DESTROYED
    }

    private fun resetBlocks() {
        repeat(3) {
            Game.world.getBlockAt(pos.copy(x = pos.x + 1, y = pos.y + it)).setType(Material.AIR)
            Game.world.getBlockAt(pos.copy(x = pos.x - 1, y = pos.y + it)).setType(Material.AIR)
            Game.world.getBlockAt(pos.copy(z = pos.z + 1, y = pos.y + it)).setType(Material.AIR)
            Game.world.getBlockAt(pos.copy(z = pos.z - 1, y = pos.y + it)).setType(Material.AIR)
        }
        lever.setBlockData((lever.blockData as Switch).apply { isPowered = false })
    }

    private fun setButtonFacing(blockFace: BlockFace) {
        buttonSide = blockFace
        button!!.setBlockData((button!!.blockData as Switch).apply {
            attachedFace = FaceAttachable.AttachedFace.WALL
            facing = blockFace
        })
    }

    private fun moveButton(deltaBlockPos: BlockPos, removePrevious: Boolean = true) {
        val buttonData = button!!.blockData
        if (removePrevious) button!!.setType(Material.AIR)

        button = Game.world.getBlockAt(
            button!!.location.add(
                deltaBlockPos.x.toDouble(),
                deltaBlockPos.y.toDouble(),
                deltaBlockPos.z.toDouble()
            )
        )
        button!!.setType(Config.objBButtonMaterial)
        button!!.setBlockData(buttonData)
    }

    private fun getRotateInterval(): Double {
        return when (level) {
            0 -> Config.objBL0Interval
            1 -> Config.objBL1Interval
            2 -> Config.objBL2Interval
            else -> error("Unknown level: $level")
        }
    }

    private fun rotateButtonClockWise() {
        val deltaPos = when (buttonSide) {
            BlockFace.EAST -> BlockPos(-1, 0, 1)
            BlockFace.SOUTH -> BlockPos(-1, 0, -1)
            BlockFace.WEST -> BlockPos(1, 0, -1)
            BlockFace.NORTH -> BlockPos(1, 0, 1)
            else -> error("Unsupported block face: $this")
        }
        moveButton(deltaPos)
        setButtonFacing(buttonSide.clockWiseNext())
    }

    private fun rotateButtonCounterClockWise() {
        val deltaPos = when (buttonSide) {
            BlockFace.EAST -> BlockPos(-1, 0, -1)
            BlockFace.SOUTH -> BlockPos(1, 0, -1)
            BlockFace.WEST -> BlockPos(1, 0, 1)
            BlockFace.NORTH -> BlockPos(-1, 0, 1)
            else -> error("Unsupported block face: $this")
        }
        moveButton(deltaPos)
        setButtonFacing(buttonSide.counterClockWiseNext())
    }

    private fun rotateButton() {
        when (level) {
            0 -> rotateButtonClockWise()
            1 -> rotateButtonCounterClockWise()
            2 -> rotateButtonClockWise()
        }
    }

    private fun plusLevel(gp: GamePlayer) {
        level++
        if (level >= 3) {
            gp.addGameItem(gameItem)
            state = COMPLETED
        } else {
            moveButton(BlockPos(0, 1, 0), false)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock == lever) {
            if (state == INACTIVATED) {
                state = ACTIVATED
            } else {
                event.cancel()
            }
        } else if (state == ACTIVATED && clickedBlock == button) {
            val gp = event.player.getGP() ?: return
            plusLevel(gp)
        }
    }

    @EventHandler
    fun onArrowLand(event: EntityInteractEvent) {
        button ?: return
        if (state == ACTIVATED && event.block == button) {
            val gp = (((event.entity as? Arrow)?.shooter) as? Player)?.getGP() ?: return
            plusLevel(gp)
        }
    }
}
