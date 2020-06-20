package site.pegasis.mc.deceit.gameitem

import kotlinx.coroutines.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import site.pegasis.mc.deceit.*

class Torch : GameItem(
    ItemStack(Config.torchMaterial).apply {
        amount = 64
        rename("Torch")
        setItemMeta(itemMeta.apply {
            (this as Damageable).damage = 10
        })
    }
) {
    private enum class State {
        ON,
        OFF,
        REMOVED
    }

    private var torchLightBlock: Block? = null
    private var reduceCountJob: Job? = null
    private val durationPerCount = (Config.torchDuration / 64 * 1000).toLong()
    private var state = State.OFF
        set(value) {
            if (value == field) return
            if (field == State.OFF && value == State.ON) {
                reduceCountJob = GlobalScope.launch {
                    while (itemStack.amount > 0 && isActive) {
                        itemStack.amount--
                        Game.plugin.inMainThread {
                            gp!!.updateGameItemToHotBar()
                        }
                        delay(durationPerCount)
                    }
                    if (!isActive) return@launch

                    Game.plugin.inMainThread {
                        state = State.REMOVED
                        torchUpdate()
                    }
                }
            } else if (field == State.ON && value == State.OFF) {
                reduceCountJob!!.cancel()
                reduceCountJob = null
            } else if (field == State.ON && value == State.REMOVED) {
                HandlerList.unregisterAll(this)
                reduceCountJob!!.cancel()
                reduceCountJob = null
            } else {
                error("Unknown state change: $field to $value")
            }
            field = value
        }

    private fun torchUpdate() {
        val player = gp?.player ?: return
        if (state == State.ON) {
            val lightBlock = player.rayTraceBlocks(Config.torchDistance)?.adjacentBlock() ?: player.rayTraceEndBlock(
                Config.torchDistance
            )
            if (lightBlock == torchLightBlock) return

            torchLightBlock?.deleteLight()
            lightBlock.setLight(Config.torchBrightness)
            torchLightBlock = lightBlock

            updateLight(player.location)
        } else if (torchLightBlock != null) {
            torchLightBlock!!.deleteLight()
            torchLightBlock = null
            updateLight(player.location)
        }
    }

    override fun onAttach(gp: GamePlayer) {
        this.gp = gp
        Main.registerEvents(this)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player != gp?.player ||
            event.hand != EquipmentSlot.HAND ||
            gp?.holdingItem() != itemStack ||
            state == State.REMOVED
        ) return
        state = if (state == State.OFF) {
            State.ON
        } else {
            State.OFF
        }
        torchUpdate()
    }

    @EventHandler
    fun onSwitchSlot(event: PlayerItemHeldEvent) {
        if (event.player != gp?.player || state == State.REMOVED) return
        state = if (gp?.holdingItem(event.newSlot) == itemStack) {
            State.ON
        } else {
            State.OFF
        }
        torchUpdate()
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.player != gp?.player || state != State.ON) return
        torchUpdate()
    }
}
