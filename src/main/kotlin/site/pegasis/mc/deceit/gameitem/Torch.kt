package site.pegasis.mc.deceit.gameitem

import kotlinx.coroutines.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayerManager
import site.pegasis.mc.deceit.player.PlayerState

class Torch(amount: Int = if (debug) 10 else 64) : LightSource(
    ItemStack(Config.torchMaterial).apply {
        this.amount = amount
        rename("Torch")
    }
) {
    private enum class State {
        ON,
        OFF,
        REMOVED
    }

    private var reduceCountJob: Job? = null
    private val durationPerCount = (Config.torchDuration / 64 * 1000).toLong()
    private var state = State.OFF
        set(value) {
            if (value == field) return
            if (field == State.OFF && value == State.ON) {
                torchUpdate(value)
                torchDamageUpdate(value)
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
                torchDamageUpdate(value)
            } else if (field == State.ON && value == State.REMOVED) {
                reduceCountJob!!.cancel()
                reduceCountJob = null
                torchUpdate(State.REMOVED)
                torchDamageUpdate(State.REMOVED)
                gp!!.removeGameItem(this@Torch)
            } else {
                error("Unknown state change: $field to $value")
            }
            field = value
        }

    private fun torchUpdate(state: State = this.state) {
        if (gp == null) return

        if (state == State.ON) {
            setFacingBlockLight(Config.torchBrightness, Config.torchDistance)
        } else {
            deleteLight(Config.torchBrightness)
        }
    }

    private fun torchDamageUpdate(state: State = this.state) {
        GamePlayerManager.livingPlayers.forEach { otherGp ->
            if (otherGp == gp) return@forEach
            if (state != State.ON) {
                changeStunLevelJobs.remove(otherGp)?.cancel()
            } else {
                if (inLightRange(otherGp.player.eyeLocation, Config.torchAngle, Config.torchDistance)) {
                    if (otherGp.state == PlayerState.TRANSFORMED && otherGp !in changeStunLevelJobs) {
                        changeStunLevelJobs[otherGp] = launchStunLevelJob(otherGp, 1, 5000L, 500L)!!
                    }
                } else {
                    changeStunLevelJobs.remove(otherGp)?.cancel()
                }
            }
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
        if (state != State.ON) return
        if (event.player == gp?.player) {
            torchUpdate()
            torchDamageUpdate()
        }
    }
}
