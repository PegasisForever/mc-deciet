package site.pegasis.mc.deceit

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.random.Random
import site.pegasis.mc.deceit.GameItemType.*

fun ItemStack.rename(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name)
    meta.lore?.clear()
    meta.isUnbreakable = true
    meta.addItemFlags(*ItemFlag.values())
    setItemMeta(meta)
}

enum class GameItemType {
    TRANSFORM_ITEM,
    CROSSBOW,
    FUSE,
    AMMO,
    TRACKER,
    ARMOR,
    CAMERA,
    INSPECTION_KIT,
    HEALTH_PACK,
    ANTIDOTE,
    LETHAL_INJECTION,
    TORCH
}

fun GameItemType.getItem(infected: Boolean? = null, count: Int = 1) = when (this) {
    TRANSFORM_ITEM -> ItemStack(Config.transformMaterial).apply {
        if (infected == true) {
            rename("Right Click to Transform")
        } else {
            rename("[FOR INFECTED] Right Click to Transform")
        }
    }
    CROSSBOW -> ItemStack(Material.CROSSBOW).apply {
        rename("Crossbow")
        addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 4)
    }
    FUSE -> ItemStack(Config.fuseMaterial).apply {
        rename("Fuse")
    }
    AMMO -> ItemStack(Material.ARROW).apply {
        rename("Arrow")
        amount = count
    }
    TRACKER -> ItemStack(Config.trackerMaterial).apply {
        rename("Tracker")
    }
    ARMOR -> ItemStack(Material.IRON_CHESTPLATE).apply {
        rename("Armor")
    }
    CAMERA -> ItemStack(Config.cameraMaterial).apply {
        rename("Camera")
    }
    INSPECTION_KIT -> ItemStack(Config.inspectionKitMaterial).apply {
        rename("Inspection Kit")
    }
    HEALTH_PACK -> ItemStack(Config.healthPackMaterial).apply {
        rename("Health Pack")
    }
    ANTIDOTE -> ItemStack(Config.antidoteMaterial).apply {
        rename("Antidote")
    }
    LETHAL_INJECTION -> ItemStack(Config.lethalInjectionMaterial).apply {
        rename(ChatColor.RED.toString() + "PUFFER FISH")
    }
    TORCH -> ItemStack(Config.torchMaterial).apply {
        rename("Torch")
    }
}

fun ItemStack.getGameItemType()=GameItem.getType(this)

object GameItem {
    fun getType(item: ItemStack): GameItemType? {
        return when (item.type) {
            Config.transformMaterial -> TRANSFORM_ITEM
            Material.CROSSBOW -> CROSSBOW
            Config.fuseMaterial -> FUSE
            Material.ARROW -> AMMO
            Config.trackerMaterial -> TRACKER
            Material.IRON_CHESTPLATE -> ARMOR
            Config.cameraMaterial -> CAMERA
            Config.inspectionKitMaterial -> INSPECTION_KIT
            Config.healthPackMaterial -> HEALTH_PACK
            Config.antidoteMaterial -> ANTIDOTE
            Config.lethalInjectionMaterial -> LETHAL_INJECTION
            Config.torchMaterial -> TORCH
            else -> null
        }
    }

    fun getRandomObjectiveItem(): ItemStack = when (Random.nextInt(6)) {
        0 -> ANTIDOTE.getItem()
        1 -> CAMERA.getItem()
        2 -> INSPECTION_KIT.getItem()
        3 -> LETHAL_INJECTION.getItem()
        4 -> TRACKER.getItem()
        5 -> TORCH.getItem()
        else -> error("wtf")
    }
}
