package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.*
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin

data class BloodPack(val itemFrame: ItemFrame, var refillJob: Job? = null)

// fixme two blood packs when itemframe facing wall
object BloodPacks {
    val list = arrayListOf<BloodPack>()

    fun hook() {
        Game.addListener(GameEvent.ON_LEVEL_START) {
            val world = Bukkit.getWorld(Config.worldName)!!
            val locations = Game.level.bloodPackPoses.map { it.toLocation() }.shuffled()
            val addLocations = locations.take(Game.level.bloodPackPosesCount).toMutableSet()
            val removeLocations = locations.takeLast(locations.size - Game.level.bloodPackPosesCount)
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
                pack.refillJob?.cancel()
            }
            list.clear()

            val world = Bukkit.getWorld(Config.worldName)!!
            val itemFrames = world.getEntitiesByClass(ItemFrame::class.java).toMutableSet()
            Game.level.bloodPackPoses.map { it.toLocation() }.forEach { location ->
                val itemFrame = itemFrames.find {
                    location.distanceSquared(it.location) < 0.3
                } ?: world.spawn(location, ItemFrame::class.java)
                itemFrame.setItem(getBloodItemStack(), false)
                itemFrame.rotation = Rotation.NONE
            }
        }
    }

    private fun addBloodPack(itemFrame: ItemFrame) {
        list.add(BloodPack(itemFrame))
        itemFrame.isInvulnerable = true
        itemFrame.setItem(getBloodItemStack(), false)
        itemFrame.rotation = Rotation.NONE
    }

    fun drink(player: Player, itemFrame: ItemFrame, plugin: JavaPlugin) {
        val found = list.find { it.itemFrame == itemFrame } ?: return
        player.getGP()!!.bloodLevel += if (Game.state == GameState.DARK) 1 else 2

        itemFrame.setItem(ItemStack(Material.GLASS_BOTTLE), false)
        itemFrame.world.playSound(
            itemFrame.location,
            Sound.ITEM_BUCKET_EMPTY_LAVA,
            SoundCategory.BLOCKS,
            1f, 1f
        )

        found.refillJob = GlobalScope.launch {
            delay(Config.bloodPackRestoreTime * 1000L)
            plugin.inMainThread {
                itemFrame.setItem(getBloodItemStack(), false)
            }
        }
    }

    private fun getBloodItemStack() = ItemStack(Material.POTION).apply {
        this.setItemMeta((this.itemMeta as PotionMeta).apply {
            color = Color.RED
        })
    }
}
