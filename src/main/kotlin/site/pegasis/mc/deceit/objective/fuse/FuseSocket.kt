package site.pegasis.mc.deceit.objective.fuse

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.EndPortalFrame
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.environment.ConsistentFallingBlock
import site.pegasis.mc.deceit.runDelayed

class FuseSocket(val block: Block, val fallingBlock: ConsistentFallingBlock) {
    var filled: Boolean = false
        set(value) {
            if (value) {
                val blockData = fallingBlock.block.blockData as EndPortalFrame
                blockData.setEye(true)

                fallingBlock.remove()
                FuseSocketManager.availableSockets.remove(this)

                FuseSocketManager.plugin.runDelayed(
                    Config.removeEntityWaitSecond
                ) {
                    block.setType(Material.END_PORTAL_FRAME)
                    block.setBlockData(blockData)
                }

                FuseSocketManager.filledSockets++
                field = true
            }
        }
}
