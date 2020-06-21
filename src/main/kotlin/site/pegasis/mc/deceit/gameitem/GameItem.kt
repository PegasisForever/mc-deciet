package site.pegasis.mc.deceit.gameitem

import org.bukkit.Material
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.gameitem.GameItemType.ARMOR
import site.pegasis.mc.deceit.gameitem.GameItemType.HEALTH_PACK
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
    ARMOR -> ItemStack(Material.IRON_CHESTPLATE).apply {
        rename("Armor")
    }
    HEALTH_PACK -> ItemStack(Config.healthPackMaterial).apply {
        rename("Health Pack")
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

    fun isHolding(slot: Int? = slotIndex) = slot != null && gp!!.player.inventory.heldItemSlot == slot

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
            // todo LETHAL_INJECTION only appear when level config allows
            0 -> Antidote()
            1 -> Camera(6)
            2 -> InspectionKit()
            3 -> LethalInjection()
            4 -> Tracker()
            5 -> Torch()
            else -> Torch()
        }
    }
}
