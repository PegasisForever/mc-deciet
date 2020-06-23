package site.pegasis.mc.deceit.gameitem

import kotlinx.coroutines.*
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.beykerykt.lightapi.LightAPI
import ru.beykerykt.lightapi.LightType
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.player.GamePlayer
import site.pegasis.mc.deceit.player.PlayerState
import kotlin.math.pow

abstract class LightSource(itemStack: ItemStack) : GameItem(itemStack) {
    protected var changeStunLevelJobs = hashMapOf<GamePlayer, Job>()
    private var lightBlock: Block? = null

    init {
        Game.addListener(GameEvent.ON_END) {
            changeStunLevelJobs.forEach { (_, job) ->
                job.cancel()
            }
        }
    }

    protected fun launchStunLevelJob(
        otherGp: GamePlayer,
        level: Int,
        restoreDuration: Long,
        repeatDuration: Long? = null
    ): Job? {
        return if (otherGp.state != PlayerState.TRANSFORMED) {
            null
        } else {
            GlobalScope.launch {
                while (isActive) {
                    GlobalScope.launch {
                        Game.plugin.inMainThread { otherGp.stunLevel += level }
                        delay(restoreDuration)
                        Game.plugin.inMainThread { otherGp.stunLevel -= level }
                    }
                    if (repeatDuration != null) {
                        delay(repeatDuration)
                    } else {
                        break
                    }
                }
            }
        }
    }

    // use eye location
    protected fun inLightRange(target: Location, angle: Double, distance: Double): Boolean {
        val playerVector = gp!!.player.eyeLocation.toVector()
        val playerLookVector = gp!!.player.location.direction
        playerVector.subtract(playerLookVector)
        val targetVector = target.toVector()

        val targetInPlayersEyeVector = targetVector.clone().subtract(playerVector)
        val degree = playerLookVector.angle(targetInPlayersEyeVector).toDegree()

        return degree < angle / 2 && targetInPlayersEyeVector.lengthSquared() < (distance + 1).pow(2)
    }

    private fun Player.rayTraceEndBlock(distance: Double): Block {
        val tracedDirection = eyeLocation.direction.clone()
            .normalize()
            .multiply(distance)
        return world.getBlockAt(eyeLocation.clone().add(tracedDirection))
    }

    private fun facingBlock(distance: Double) =
        gp?.player?.rayTraceBlocks(distance)?.adjacentBlock() ?: gp?.player?.rayTraceEndBlock(distance)

    private fun updateLight(level: Int, location: Location) {
        LightAPI.collectChunks(location, LightType.BLOCK, level).forEach {
            LightAPI.updateChunk(it, LightType.BLOCK)
        }
    }

    protected fun setFacingBlockLight(level: Int, distance: Double) {
        val facingBlock = facingBlock(distance) ?: return
        if (facingBlock == lightBlock) return

        lightBlock?.let { LightAPI.deleteLight(it.location, LightType.BLOCK, false) }

        LightAPI.createLight(facingBlock.location, LightType.BLOCK, level, false)
        lightBlock = facingBlock

        updateLight(level, lightBlock!!.location)
    }

    protected fun deleteLight(level: Int) {
        if (lightBlock == null) return

        val blockLocation = lightBlock!!.location
        LightAPI.deleteLight(lightBlock!!.location, LightType.BLOCK, false)
        lightBlock = null

        updateLight(level, blockLocation)
    }
}
