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

object BloodPacks {
    val list = arrayListOf<BloodPack>()
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun loadAll(world: World) {
        list.clear()
        world.getEntitiesByClass(ItemFrame::class.java).forEach { add(it) }
    }

    private fun add(itemFrame: ItemFrame) {
        list.add(BloodPack(itemFrame))
        itemFrame.isInvulnerable = true
        itemFrame.setItem(getBloodItemStack(), false)
        itemFrame.rotation = Rotation.NONE
    }

    fun drink(player: Player, itemFrame: ItemFrame) {
        val found = list.find { it.itemFrame == itemFrame } ?: return
        GamePlayer.get(player)!!.addBloodLevel(if (GameState.dark) 1 else 2)

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

    fun gameEnd() {
        list.forEach { pack ->
            pack.refillJob?.cancel()
        }
    }

    private fun getBloodItemStack() = ItemStack(Material.POTION).apply {
        (this.itemMeta as PotionMeta).color = Color.RED
    }
}
