package site.pegasis.mc.deceit.gameitem

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.player.GamePlayer
import site.pegasis.mc.deceit.rename

class Armor : GameItem(
    ItemStack(Material.IRON_CHESTPLATE).apply {
        rename("Armor")
    }
) {
    override fun onAttach(gp: GamePlayer, index: Int) {
        super.onAttach(gp, index)
        if (!gp.hasArmor) {
            gp.player.inventory.chestplate = ItemStack(Material.IRON_CHESTPLATE)
            gp.player.inventory.leggings = ItemStack(Material.IRON_LEGGINGS)
        }
        modifyItemStack { amount = 0 }
    }
}
