package site.pegasis.mc.deceit

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

fun ItemStack.rename(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name)
    setItemMeta(meta)
}

object GameItem {
    fun getTransformItem(infected: Boolean) = ItemStack(Config.transformMaterial).apply {
        if (infected) {
            rename("Right Click to Transform")
        } else {
            rename("[FOR INFECTED] Right Click to Transform")
        }
    }

    fun getHandGun() = ItemStack(Material.CROSSBOW).apply {
        addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 4)
    }

    fun getFuse() = ItemStack(Config.fuseMaterial).apply {
        rename("Fuse")
    }
}
