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
        amount = if (debug) 10 else 64
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
                torchUpdate(value)
                reduceCountJob = GlobalScope.launch {
                    while (getItemStack() != null && isActive) {
                        Game.plugin.inMainThread {
                            modifyItemStack {
                                amount--
                            }
                        }
                        delay(durationPerCount)
                    }
                    if (!isActive) return@launch

                    Game.plugin.inMainThread {
                        state = State.REMOVED
                    }
                }
            } else if (field == State.ON && value == State.OFF) {
                reduceCountJob!!.cancel()
                reduceCountJob = null
                torchUpdate(value)
            } else if (field == State.ON && value == State.REMOVED) {
                reduceCountJob!!.cancel()
                reduceCountJob = null
                torchUpdate(State.REMOVED)
                gp!!.removeGameItem(this@Torch)
            } else {
                error("Unknown state change: $field to $value")
            }
            field = value
        }

    private fun torchUpdate(state: State = this.state) {
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

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player != gp?.player ||
            event.hand != EquipmentSlot.HAND ||
            !isHolding() ||
            state == State.REMOVED
        ) return
        state = if (state == State.OFF) {
            State.ON
        } else {
            State.OFF
        }
    }

    @EventHandler
    fun onSwitchSlot(event: PlayerItemHeldEvent) {
        if (event.player != gp?.player || state == State.REMOVED) return
        if (!isHolding(event.newSlot)) {
            state = State.OFF
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.player != gp?.player || state != State.ON) return
        torchUpdate()
    }
}
