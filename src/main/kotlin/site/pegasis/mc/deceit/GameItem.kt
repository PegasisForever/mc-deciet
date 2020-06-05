package site.pegasis.mc.deceit

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

fun ItemStack.rename(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name)
    meta.lore?.clear()
    meta.isUnbreakable = true
    meta.addItemFlags(*ItemFlag.values())
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
        rename("Crossbow")
        addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 4)
    }

    fun getFuse() = ItemStack(Config.fuseMaterial).apply {
        rename("Fuse")
    }

    fun getAmmo(count: Int) = ItemStack(Material.ARROW).apply {
        rename("Arrow")
        amount = count
    }

    fun getTracker() = ItemStack(Material.STONE_BUTTON).apply {
        rename("Tracker")
    }

    fun getArmor() = ItemStack(Material.IRON_CHESTPLATE).apply {
        rename("Armor")
    }

    fun getCamera() = ItemStack(Material.ITEM_FRAME).apply {
        rename("Camera")
    }

    fun getInspectionKit() = ItemStack(Material.DAYLIGHT_DETECTOR).apply {
        rename("Inspection Kit")
    }

    fun getHealthPack() = ItemStack(Material.GOLDEN_APPLE).apply {
        rename("Health Pack")
    }

    fun getAntidote() = ItemStack(Material.EMERALD).apply {
        rename("Antidote")
    }

    fun getLethalInjection() = ItemStack(Material.PUFFERFISH).apply {
        rename(ChatColor.RED.toString() + "PUFFER FISH")
    }

    fun getTorch() = ItemStack(Material.LEVER).apply {
        rename("Torch")
    }

    fun getRandomObjectiveItem(): ItemStack = when (Random.nextInt(6)) {
        0 -> getAntidote()
        1 -> getCamera()
        2 -> getInspectionKit()
        3 -> getLethalInjection()
        4 -> getTracker()
        5 -> getTorch()
        else -> error("wtf")
    }

}
