package site.pegasis.mc.deceit

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object GameItem {
    fun getTransformItem(infected:Boolean) = ItemStack(Config.transformMaterial).apply {
        val meta = itemMeta!!
        if (infected){
            meta.setDisplayName("Right Click to Transform")
        }else{
            meta.setDisplayName("[FOR INFECTED] Right Click to Transform")
        }
        setItemMeta(meta)
    }

    fun getHandGun() = ItemStack(Material.CROSSBOW).apply {
        addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 4)
    }
}
