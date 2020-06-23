package site.pegasis.mc.deceit.gameitem

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.rename

class Armor :GameItem(
    ItemStack(Material.IRON_CHESTPLATE).apply {
        rename("Armor")
    }
)
