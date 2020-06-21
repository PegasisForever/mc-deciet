package site.pegasis.mc.deceit.gameitem

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.player.GamePlayer
import site.pegasis.mc.deceit.rename

class Arrow(amount: Int) : GameItem(
    ItemStack(Material.ARROW).apply {
        rename("Arrow")
        this.amount = amount
    }
) {
    override fun onAttach(gp: GamePlayer, index: Int) {
        super.onAttach(gp, index)
        for (oldArrow: GameItem? in gp.gameItems) {
            if (oldArrow is Arrow) {
                val newAmount = oldArrow.getItemStack()!!.amount + getItemStack()!!.amount
                if (newAmount <= 64) {
                    oldArrow.modifyItemStack {
                        amount = newAmount
                    }
                    this.modifyItemStack {
                        amount = 0
                    }
                } else {
                    oldArrow.modifyItemStack {
                        amount = 64
                    }
                    this.modifyItemStack {
                        amount = newAmount - 64
                    }
                }
                break
            }
        }
    }
}
