package site.pegasis.mc.deceit.gameitem

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.gameitem.GameItemType.*
import kotlin.random.Random

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
    private val itemStack: ItemStack,
    var gp: GamePlayer? = null,
    var slotIndex: Int? = null
) : Listener {
    init {
        Game.addListener(GameEvent.ON_END) {
            HandlerList.unregisterAll(this@GameItem)
        }
    }

    fun getItemStack() = if (gp == null) itemStack else gp!!.player.inventory.getItem(slotIndex!!)

    fun modifyItemStack(action: ItemStack.() -> Unit) {
        val itemStack = getItemStack()!!
        action(itemStack)
        setItemStack(itemStack)
    }

    fun setItemStack(itemStack: ItemStack?) {
        gp!!.player.inventory.setItem(slotIndex!!, itemStack)
    }

    open fun onAttach(gp: GamePlayer, index: Int) {
        slotIndex = index
        this.gp = gp
        setItemStack(itemStack)
        Main.registerEvents(this)
    }

    open fun onDetach() {
        HandlerList.unregisterAll(this)
        setItemStack(null)
        gp = null
        slotIndex = null
    }

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
