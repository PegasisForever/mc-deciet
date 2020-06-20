package site.pegasis.mc.deceit.objective

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.gameitem.GameItem
import site.pegasis.mc.deceit.objective.ObjectiveC.State.*

class ObjectiveC(
    pos: BlockPos,
    leverPos: BlockPos,
    pressurePlatePos: BlockPos,
    private val gameItem: GameItem
) : Objective {
    private enum class State {
        INACTIVATED,
        CLOSED,
        OPENED,
        COMPLETED,
        DESTROYED
    }

    private val pressurePlate = Game.world.getBlockAt(pressurePlatePos)
    private val lever = Game.world.getBlockAt(leverPos)
    private val closeBlock = Game.world.getBlockAt(pos.copy(y = pos.y + 1))
    private val openBlock = Game.world.getBlockAt(pos.copy(y = pos.y + 2))
    private val itemFrameLocation = pos.toEntityPos().toLocation().add(Location(Game.world, 0.5, 1.03125, 0.5))
    private var itemFrame: ItemFrame? = null

    private val insidePlayers = arrayListOf<GamePlayer>()

    // state setter use only
    private fun open() {
        openBlock.setType(Config.objCCoverMaterial)
        closeBlock.setType(Material.AIR)

        itemFrame = Game.world.spawn(itemFrameLocation, ItemFrame::class.java) {
            it.setFacingDirection(BlockFace.UP)
            it.setItem(gameItem.itemStack.clone())
            it.isInvulnerable = true
        }
    }

    // state setter use only
    private fun close() {
        itemFrame?.remove()
        itemFrame = null
        Game.plugin.runDelayed(Config.removeEntityWaitSecond) {
            closeBlock.setType(Config.objCCoverMaterial)
            openBlock.setType(Material.AIR)
        }
    }

    // state setter use only
    private fun complete() {
        itemFrame?.setItem(null)
        insidePlayers.forEach { it.lockGetItem = false }
        insidePlayers.clear()
    }

    // state setter use only
    private fun destroy() {
        HandlerList.unregisterAll(this)
        itemFrame?.remove()
        itemFrame = null
        Game.plugin.runDelayed(Config.removeEntityWaitSecond) {
            resetBlocks(true)
        }
    }

    private var state = INACTIVATED
        set(value) {
            if (value == field) return
            if (value == DESTROYED) {
                complete()
                destroy()
            } else if (field == INACTIVATED && value == CLOSED) {
                pressurePlate.setType(Config.objCPressurePlateMaterial)
            } else if (field == CLOSED && value == OPENED) {
                open()
            } else if (field == OPENED && value == CLOSED) {
                close()
            } else if (field == OPENED && value == COMPLETED) {
                complete()
            } else {
                error("Unknown state change: $field to $value")
            }
            field = value
        }

    init {
        resetBlocks(false)
    }

    private fun resetBlocks(setPressurePlate: Boolean) {
        closeBlock.setType(Config.objCCoverMaterial)
        openBlock.setType(Material.AIR)
        lever.setBlockData((lever.blockData as Switch).apply { isPowered = false })
        if (setPressurePlate) {
            pressurePlate.setType(Config.objCPressurePlateMaterial)
        } else {
            pressurePlate.setType(Material.AIR)
        }
    }

    override fun destroyAndReset() {
        state = DESTROYED
    }

    fun take(frame: ItemFrame, gp: GamePlayer): Boolean {
        if (state != OPENED || frame != itemFrame || gp in insidePlayers) return false
        gp.addGameItem(gameItem)
        state = COMPLETED
        return true
    }

    @EventHandler(ignoreCancelled = true)
    fun onLeverPull(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock == lever) {
            if (state == INACTIVATED) {
                state = CLOSED
            } else {
                event.cancel()
            }
        }
    }

    private fun Location.isInPressurePlate(): Boolean {
        val pressurePlatePos = pressurePlate.blockPos
        return x < pressurePlatePos.x + 0.5 + 0.65 &&
                x > pressurePlatePos.x + 0.5 - 0.65 &&
                z < pressurePlatePos.z + 0.5 + 0.65 &&
                z > pressurePlatePos.z + 0.5 - 0.65 &&
                y < pressurePlatePos.y + 0.5 &&
                y > pressurePlatePos.y - 0.5
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (state != OPENED && state != CLOSED) return
        val gp = event.player.getGP() ?: return
        if (gp.state == PlayerState.DEAD) return
        if (event.player.location.isInPressurePlate()) {
            if (gp !in insidePlayers) {
                insidePlayers += gp
                gp.lockGetItem = true
                state = OPENED
            }
        } else {
            if (insidePlayers.remove(gp)) {
                state = if (insidePlayers.isEmpty()) CLOSED else OPENED
                gp.lockGetItem = false
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onRightClick(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.rightClicked != itemFrame) return
        if (state != OPENED) return
        if (!Game.started) return
        val player = event.player
        val gp = player.getGP() ?: return
        if (gp in insidePlayers) return

        event.cancel()

        gp.addGameItem(gameItem)
        state = COMPLETED
    }
}
