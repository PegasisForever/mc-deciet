package site.pegasis.mc.deceit.environment

import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent
import site.pegasis.mc.deceit.gameitem.Armor
import site.pegasis.mc.deceit.gameitem.Arrow
import site.pegasis.mc.deceit.gameitem.HealthPack
import site.pegasis.mc.deceit.rename
import kotlin.random.Random

object DroppedItemManager {
    private val droppedItems = arrayListOf<Entity>()

    fun hook() {
        Game.addListener(GameEvent.ON_LEVEL_START) {
            Game.level.itemSpawnPlaces
                .shuffled()
                .take(Game.level.itemSpawnCount)
                .forEach { pos ->
                    droppedItems += Game.world.dropItem(pos.toLocation(), getRandomDroppedItem().getItemStack()!!)
                        .apply { pickupDelay = Int.MAX_VALUE - 100 }
                }
        }
        Game.addListener(GameEvent.ON_LEVEL_END) {
            droppedItems.forEach {
                it.remove()
            }
            droppedItems.clear()
        }
    }

    private fun getRandomDroppedItem() = when (Random.nextInt(3)) {
        0 -> Armor()
        1 -> HealthPack()
        2 -> Arrow(8)
        else -> error("wtf")
    }

    fun isRandomDroppedItem(material: Material) =
        material == Material.IRON_CHESTPLATE || material == Config.healthPackMaterial || material == Material.ARROW

    fun Item.getGameItem() = when (itemStack.type) {
        Material.IRON_CHESTPLATE -> Armor()
        Config.healthPackMaterial -> HealthPack()
        Material.ARROW -> Arrow(8)
        else -> error("Can't convert ${itemStack.type} to game item.")
    }
}
