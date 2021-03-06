package site.pegasis.mc.deceit.gameitem

import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayer
import site.pegasis.mc.deceit.player.PlayerState

class TransformItem(private val isInfected: Boolean) : GameItem(
    ItemStack(Config.transformMaterial).apply {
        if (isInfected) {
            rename("Right Click to Transform")
        } else {
            rename("[FOR INFECTED] Right Click to Transform")
        }
    }
) {
    override fun onAttach(gp: GamePlayer, index: Int) {
        super.onAttach(gp, index)
        Game.addListener(GameEvent.ON_SECOND) {
            if (gp.canTransform && getItemStack()?.enchantments?.isEmpty() == true) {
                modifyItemStack {
                    enchant()
                }
            } else if (!gp.canTransform && getItemStack()?.enchantments?.isNotEmpty() == true) {
                modifyItemStack {
                    removeEnchant()
                }
            }
        }
    }

    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.player != gp?.player) return

        if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) &&
            isHolding() &&
            gp!!.canTransform
        ) {
            gp!!.state = PlayerState.TRANSFORMED
        }
    }
}
