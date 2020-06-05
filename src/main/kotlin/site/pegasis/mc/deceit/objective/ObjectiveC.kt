package site.pegasis.mc.deceit.objective

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*

class ObjectiveC(
    pos: BlockPos,
    leverPos: BlockPos,
    val pressurePlatePos: BlockPos,
    val gameItem: ItemStack,
    val plugin: JavaPlugin
) : Listener,
    Objective {
    val insidePlayers = arrayListOf<GamePlayer>()
    private var distroyed = false
    val world = Bukkit.getWorld(Config.worldName)!!
    val pressurePlate = world.getBlockAt(pressurePlatePos)
    val lever = world.getBlockAt(leverPos)
    val closeBlock = world.getBlockAt(pos.copy(y = pos.y + 1))
    val openBlock = world.getBlockAt(pos.copy(y = pos.y + 2))
    val itemFrameLocation = pos.toEntityPos().toLocation(world).add(Location(world, 0.5, 1.03125, 0.5))
    var itemFrame: ItemFrame? = null
    var activated = false
    private var opened = false
    var taken = false
    val completed: Boolean
        get() = taken

    init {
        pressurePlate.setType(Material.AIR)
        powerOffLever()
        resetBlocks()
    }

    fun take(frame: ItemFrame, gp: GamePlayer):Boolean {
        if (completed || gp.lockGetItem || frame != itemFrame) return false
        gp.addGameItem(gameItem.clone())
        itemFrame?.setItem(null)
        taken = true
        return true
    }

    private fun updateCoverBlocks(isOpen: Boolean, force: Boolean = false) {
        if (opened == isOpen && !force) return
        if (isOpen) {
            openBlock.setType(Config.objCCoverMaterial)
            closeBlock.setType(Material.AIR)

            itemFrame = world.spawn(itemFrameLocation, ItemFrame::class.java)
            itemFrame?.setFacingDirection(BlockFace.UP)
            if (!taken) {
                itemFrame?.setItem(gameItem.clone())
            }
            itemFrame?.isInvulnerable = true
        } else {
            itemFrame?.remove()
            itemFrame = null

            plugin.runDelayed(Config.removeEntityWaitSecond) {
                closeBlock.setType(Config.objCCoverMaterial)
                openBlock.setType(Material.AIR)
            }
        }
        opened = isOpen
    }

    private fun resetBlocks() {
        updateCoverBlocks(false, true)
    }

    private fun powerOffLever() {
        lever.setBlockData((lever.blockData as Switch).apply { isPowered = false })
    }

    override fun destroy() {
        distroyed = true
        resetBlocks()
        powerOffLever()
        pressurePlate.setType(Config.objCPressurePlateMaterial)
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
                pressurePlate.setType(Config.objCPressurePlateMaterial)
            }
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!Game.started || distroyed || !activated || completed) return
        val gp = event.player.getGP() ?: return
        val location = event.player.location
        if (location.x < pressurePlatePos.x + 0.5 + 0.65 &&
            location.x > pressurePlatePos.x + 0.5 - 0.65 &&
            location.z < pressurePlatePos.z + 0.5 + 0.65 &&
            location.z > pressurePlatePos.z + 0.5 - 0.65 &&
            location.y < pressurePlatePos.y + 0.5 &&
            location.y > pressurePlatePos.y - 0.5
        ) {
            if (gp !in insidePlayers) {
                insidePlayers += gp
                gp.lockGetItem = true
                updateCoverBlocks(insidePlayers.isNotEmpty())
            }
        } else {
            if (insidePlayers.remove(gp)) {
                updateCoverBlocks(insidePlayers.isNotEmpty())
                gp.lockGetItem = false
            }
        }
    }
}
