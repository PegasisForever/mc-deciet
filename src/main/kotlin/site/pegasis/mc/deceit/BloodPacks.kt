package site.pegasis.mc.deceit

import org.bukkit.*
import org.bukkit.entity.ItemFrame
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta

data class BloodPack(val itemFrame: ItemFrame)

object BloodPacks {
    val list = arrayListOf<BloodPack>()
    fun loadAll(world: World) {
        list.clear()
        world.getEntitiesByClass(ItemFrame::class.java).forEach { add(it) }
    }

    private fun add(itemFrame: ItemFrame) {
        list.add(BloodPack(itemFrame))
        itemFrame.isInvulnerable = true
        itemFrame.setItem(ItemStack(Material.POTION).apply {
            (this.itemMeta as PotionMeta).color = Color.RED
        }, false)
        itemFrame.rotation = Rotation.NONE
    }

    fun drink(itemFrame: ItemFrame) {
        val found = list.find { it.itemFrame == itemFrame } ?: return
        itemFrame.setItem(ItemStack(Material.GLASS_BOTTLE), false)
        itemFrame.world.playSound(
            itemFrame.location,
            Sound.ITEM_BUCKET_EMPTY_LAVA,
            SoundCategory.BLOCKS,
            1f, 1f
        )
    }
}
