package site.pegasis.mc.deceit.gameitem

import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*

class TransformItem(private val isInfected: Boolean) : GameItem(
    ItemStack(Config.transformMaterial).apply {
        if (isInfected) {
            rename("Right Click to Transform")
        } else {
            rename("[FOR INFECTED] Right Click to Transform")
        }
    }
) {
    override fun onAttach(gp: GamePlayer) {
        this.gp = gp
        Main.registerEvents(this)
        Game.addListener(GameEvent.ON_SECOND) {
            if (gp.canTransform()) {
                itemStack.enchant()
            } else {
                itemStack.removeEnchant()
            }
            gp.updateGameItemToHotBar()
        }
    }

    @EventHandler
    fun onRightClick(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.player != gp?.player) return

        val itemInHand = event.player.inventory.itemInMainHand
        if ((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) &&
            itemInHand.type == Config.transformMaterial &&
            gp!!.canTransform()
        ) {
            gp!!.state = PlayerState.TRANSFORMED
        }
    }
}
