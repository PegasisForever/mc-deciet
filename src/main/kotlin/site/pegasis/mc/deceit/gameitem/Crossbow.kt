package site.pegasis.mc.deceit.gameitem

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.rename

class Crossbow : GameItem(
    ItemStack(Material.CROSSBOW).apply {
        rename("Crossbow")
        addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 4)
    }
)
