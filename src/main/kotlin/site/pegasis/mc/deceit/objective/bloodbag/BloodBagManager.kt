package site.pegasis.mc.deceit.objective.bloodbag

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.entity.ItemFrame
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent

// fixme two blood packs when itemframe facing wall
object BloodBagManager {
    val list = arrayListOf<BloodBag>()

    fun hook() {
        Game.addListener(GameEvent.ON_LEVEL_START) {
            val world = Bukkit.getWorld(Config.worldName)!!
            val locations =
                Game.level.bloodBagPoses.map { it.toLocation() }.shuffled()
            val addLocations =
                locations.take(Game.level.bloodBagPosesCount).toMutableSet()
            val removeLocations =
                locations.takeLast(locations.size - Game.level.bloodBagPosesCount)
            val itemFrames = world.getEntitiesByClass(ItemFrame::class.java).toMutableSet()

            removeLocations.forEach { removeLocation ->
                val itemFrame = itemFrames.find { removeLocation.distanceSquared(it.location) < 0.3 } ?: return@forEach
                itemFrame.remove()
                itemFrames.remove(itemFrame)
            }
            itemFrames.forEach { itemFrame ->
                val location = addLocations.find { it.distanceSquared(itemFrame.location) < 0.3 } ?: return@forEach
                addBloodPack(itemFrame)
                addLocations.remove(location)
            }
            addLocations.forEach { location ->
                val itemFrame = world.spawn(location, ItemFrame::class.java)
                addBloodPack(itemFrame)
            }
        }

        Game.addListener(GameEvent.ON_LEVEL_END) {
            list.forEach { pack ->
                pack.destroy()
            }
            list.clear()

            val world = Bukkit.getWorld(Config.worldName)!!
            val itemFrames = world.getEntitiesByClass(ItemFrame::class.java).toMutableSet()
            Game.level.bloodBagPoses.map { it.toLocation() }
                .forEach { location ->
                    val itemFrame = itemFrames.find {
                        location.distanceSquared(it.location) < 0.3
                    } ?: world.spawn(location, ItemFrame::class.java)
                    itemFrame.setItem(
                        getBloodItemStack(),
                        false
                    )
                    itemFrame.rotation = Rotation.NONE
                }
        }
    }

    private fun addBloodPack(itemFrame: ItemFrame) {
        list.add(BloodBag(itemFrame))
        itemFrame.isInvulnerable = true
        itemFrame.setItem(getBloodItemStack(), false)
        itemFrame.rotation = Rotation.NONE
    }

    fun getBloodItemStack() = ItemStack(Material.POTION).apply {
        this.setItemMeta((this.itemMeta as PotionMeta).apply {
            color = Color.RED
        })
    }
}
