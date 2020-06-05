package site.pegasis.mc.deceit.objective

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*

class ObjectiveB(
    val pos: BlockPos,
    leverPos: BlockPos,
    itemFrameBlockPos: BlockPos,
    val gameItem: ItemStack,
    val plugin: JavaPlugin
) : Listener,
    Objective {
    private var distroyed = false
    val world = Bukkit.getWorld(Config.worldName)!!
    val lever = world.getBlockAt(leverPos)
    val itemFrame: ItemFrame
    val rotateJob: Job
    var activated = false
    var level = 0
    var button: Block? = null
    lateinit var buttonSide: BlockFace
    val completed: Boolean
        get() = level == 3

    private fun getRotateInterval(): Double {
        return when (level) {
            0 -> Config.objBL0Interval
            1 -> Config.objBL1Interval
            2 -> Config.objBL2Interval
            else -> error("Unknown level: $level")
        }
    }

    private fun powerOffLever() {
        lever.setBlockData((lever.blockData as Switch).apply { isPowered = false })
    }

    init {
        resetBlocks()
        powerOffLever()
        val itemFramePos = itemFrameBlockPos.toEntityPos()
            .copy(y = itemFrameBlockPos.y + 1.0, x = itemFrameBlockPos.x + 0.5, z = itemFrameBlockPos.z + 0.5)
            .toLocation(world)
        itemFrame = world.getNearbyEntities(itemFramePos, 0.3, 0.3, 0.3).first() as ItemFrame
        itemFrame.setItem(gameItem.clone())
        itemFrame.isInvulnerable = true

        rotateJob = GlobalScope.launch {
            while (!completed && isActive) {
                if (!activated) {
                    delay(1000)
                    continue
                }

                val rotateInterval = getRotateInterval()
                delay((rotateInterval * 1000).toLong())
                plugin.inMainThread {
                    rotateButton()
                }
            }
        }
    }

    override fun destroy() {
        distroyed = true
        itemFrame.setItem(null)
        resetBlocks()
        powerOffLever()
        rotateJob.cancel()
    }

    fun resetBlocks() {
        repeat(3) {
            world.getBlockAt(pos.copy(x = pos.x + 1, y = pos.y + it)).setType(Material.AIR)
            world.getBlockAt(pos.copy(x = pos.x - 1, y = pos.y + it)).setType(Material.AIR)
            world.getBlockAt(pos.copy(z = pos.z + 1, y = pos.y + it)).setType(Material.AIR)
            world.getBlockAt(pos.copy(z = pos.z - 1, y = pos.y + it)).setType(Material.AIR)
        }
    }

    fun setButtonFacing(blockFace: BlockFace) {
        buttonSide = blockFace
        button!!.setBlockData((button!!.blockData as Switch).apply {
            attachedFace = FaceAttachable.AttachedFace.WALL
            facing = blockFace
        })
    }

    fun moveButton(deltaBlockPos: BlockPos) {
        val buttonData = button!!.blockData
        button!!.setType(Material.AIR)

        button = world.getBlockAt(
            button!!.location.add(
                deltaBlockPos.x.toDouble(),
                deltaBlockPos.y.toDouble(),
                deltaBlockPos.z.toDouble()
            )
        )
        button!!.setType(Config.objBButtonMaterial)
        button!!.setBlockData(buttonData)
    }

    fun rotateButtonClockWise() {
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

    fun rotateButtonCounterClockWise() {
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

    fun rotateButton() {
        when (level) {
            0 -> rotateButtonClockWise()
            1 -> rotateButtonCounterClockWise()
            2 -> rotateButtonClockWise()
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!Game.started || distroyed) return
        if (event.clickedBlock == lever) {
            val leverData = lever.blockData as Switch
            if (leverData.isPowered) {
                event.cancel()
            } else {
                activated = true
                button = world.getBlockAt(pos.copy(x = pos.x + 1))
                button!!.setType(Config.objBButtonMaterial)
                setButtonFacing(BlockFace.EAST)
            }
        } else if (event.clickedBlock == button && !completed) {
            level++
            if (level == 3) {
                button!!.setType(Material.AIR)
                event.player.getGP()?.addGameItem(gameItem)
                itemFrame.setItem(null)
            } else {
                moveButton(BlockPos(0, 1, 0))
            }
        }
    }

    private fun BlockFace.clockWiseNext() = when (this) {
        BlockFace.EAST -> BlockFace.SOUTH
        BlockFace.SOUTH -> BlockFace.WEST
        BlockFace.WEST -> BlockFace.NORTH
        BlockFace.NORTH -> BlockFace.EAST
        else -> error("Unsupported block face: $this")
    }

    private fun BlockFace.counterClockWiseNext() = when (this) {
        BlockFace.EAST -> BlockFace.NORTH
        BlockFace.SOUTH -> BlockFace.EAST
        BlockFace.WEST -> BlockFace.SOUTH
        BlockFace.NORTH -> BlockFace.WEST
        else -> error("Unsupported block face: $this")
    }
}
