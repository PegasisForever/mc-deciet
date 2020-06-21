package site.pegasis.mc.deceit.objective.bloodbag

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayer
import site.pegasis.mc.deceit.player.GamePlayerManager.getGP

class BloodBag(private val itemFrame: ItemFrame, var refillJob: Job? = null) :
    Listener {
    init {
        Main.registerEvents(this)
    }

    fun destroy(){
        refillJob?.cancel()
        HandlerList.unregisterAll(this)
    }

    @EventHandler(ignoreCancelled = true)
    fun onRightClick(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.rightClicked != itemFrame) return
        val player = event.player
        if (!Game.started || player.gameMode == GameMode.CREATIVE) return
        val gp = player.getGP() ?: return

        event.cancel()

        val item = itemFrame.item
        if (gp.isInfected && item.type == Material.POTION) {
            drink(gp)
        } else if (gp.isInfected && item.type == Material.GLASS_BOTTLE) {
            player.sendMessage("This blood bag is empty!")
        }
    }

    private fun drink(gp: GamePlayer) {
        gp.bloodLevel += if (Game.state == GameState.DARK) 1 else 2

        itemFrame.setItem(ItemStack(Material.GLASS_BOTTLE), false)
        itemFrame.world.playSound(
            itemFrame.location,
            Sound.ITEM_BUCKET_EMPTY_LAVA,
            SoundCategory.BLOCKS,
            1f, 1f
        )

        refillJob = GlobalScope.launch {
            delay(Config.bloodPackRestoreTime * 1000L)
            Game.plugin.inMainThread {
                itemFrame.setItem(BloodBagManager.getBloodItemStack(), false)
            }
        }
    }
}
