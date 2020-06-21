package site.pegasis.mc.deceit.gameitem

import kotlinx.coroutines.*
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayerEffectFlag
import site.pegasis.mc.deceit.player.GamePlayerManager
import site.pegasis.mc.deceit.player.PlayerState

class Torch(amount: Int = if (debug) 10 else 64) : GameItem(
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

    private var torchLightBlock: Block? = null
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

    private fun torchDamageUpdate(state: State = this.state) {
        GamePlayerManager.gps.values.forEach { otherGp ->
            if (otherGp == gp) return@forEach
            val isInRange = inLightRange(otherGp.player.eyeLocation)
            if (isInRange && state == State.ON) {
                otherGp.addEffectFlag(GamePlayerEffectFlag.IN_LIGHT, this)
            } else if (!isInRange || state != State.ON) {
                otherGp.removeEffectFlag(GamePlayerEffectFlag.IN_LIGHT, this)
            }
        }
    }

    // use eye location
    private fun inLightRange(target: Location): Boolean {
        val playerVector = gp!!.player.eyeLocation.toVector()
        val playerLookVector = gp!!.player.location.direction
        playerVector.subtract(playerLookVector)
        val targetVector = target.toVector()

        val targetInPlayersEyeVector = targetVector.clone().subtract(playerVector)
        val degree = playerLookVector.angle(targetInPlayersEyeVector).toDegree()

        return degree < Config.torchAngle / 2 && targetInPlayersEyeVector.length() < Config.torchDistance
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
