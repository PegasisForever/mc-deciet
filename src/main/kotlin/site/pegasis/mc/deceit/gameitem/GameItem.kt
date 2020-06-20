package site.pegasis.mc.deceit.gameitem

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent
import site.pegasis.mc.deceit.GamePlayer
import site.pegasis.mc.deceit.gameitem.GameItemType.*
import kotlin.random.Random

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
    else -> TODO()
}

abstract class GameItem(
    val itemStack: ItemStack,
    var gp: GamePlayer? = null
) : Listener {
    init {
        Game.addListener(GameEvent.ON_END) {
            HandlerList.unregisterAll(this@GameItem)
        }
    }

    abstract fun onAttach(gp: GamePlayer)

    companion object {
        fun getRandomObjectiveItem(): GameItem = when (Random.nextInt(6)) {
            // fixme
//            0 -> ANTIDOTE.getItem()
//            1 -> CAMERA.getItem()
//            2 -> INSPECTION_KIT.getItem()
//            3 -> LETHAL_INJECTION.getItem()
//            4 -> TRACKER.getItem()
            5 -> Torch()
            else -> Torch()
        }
    }
}

