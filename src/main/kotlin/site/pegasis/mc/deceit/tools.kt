package site.pegasis.mc.deceit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.Team
import org.bukkit.util.RayTraceResult
import ru.beykerykt.lightapi.LightAPI
import ru.beykerykt.lightapi.LightType
import java.util.logging.Level
import java.util.logging.Logger

fun JavaPlugin.log(msg: Any?) {
    logger.logInfo(msg.toString())
}

fun Logger.logInfo(msg: String) {
    log(Level.INFO, msg)
}

fun JavaPlugin.runDelayed(seconds: Double, action: () -> Unit) {
    object : BukkitRunnable() {
        override fun run() {
            action()
        }
    }.runTaskLater(this, (seconds * 20).toLong())

}

suspend fun <T> JavaPlugin.inMainThread(action: () -> T): T {
    return withContext(Dispatchers.IO) {
        Bukkit.getScheduler().callSyncMethod(this@inMainThread) { action() }.get()
    }
}

fun JavaPlugin.consoleCommand(cmd: String) {
    this.server.dispatchCommand(Bukkit.getConsoleSender(), cmd)
}

fun Cancellable.cancel() {
    isCancelled = true
}

fun Chunk.forEachBlock(action: (Block) -> Unit) {
    for (x in 0..15) for (y in 0..255) for (z in 0..15) {
        action(getBlock(x, y, z))
    }
}

fun ItemStack.enchant(): ItemStack {
    setItemMeta(itemMeta!!.apply { addItemFlags(ItemFlag.HIDE_ENCHANTS) })
    addUnsafeEnchantment(Enchantment.DURABILITY, 1)
    return this
}

fun ItemStack.removeEnchant(): ItemStack {
    removeEnchantment(Enchantment.DURABILITY)
    return this
}

fun isInMainThread() = Thread.currentThread().name == "Server thread"

fun Player.getUnderBlockLocation(): Location {
    var deltaY = 0.0
    while (deltaY > -5) {
        val testLoc = location.clone().add(0.0, deltaY, 0.0)
        val block = world.getBlockAt(testLoc)
        if (block.type.isSolid) return testLoc.apply {
            y = block.location.y
        }
        deltaY--
    }
    return location
}

fun Player.hideNameTag() {
    val scoreBoard = Bukkit.getScoreboardManager().newScoreboard
    scoreBoard.registerNewTeam(name).apply {
        setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
        addEntry(name)
    }
}

fun RayTraceResult.adjacentBlock(): Block? {
    if (hitEntity != null) {
        return hitBlock!!.world.getBlockAt(hitEntity!!.location)
    } else if (hitBlock != null) {
        val adjacentPos = with(hitBlockFace!!) {
            hitBlock!!.location.clone().add(modX.toDouble(), modY.toDouble(), modZ.toDouble())
        }
        return hitBlock!!.world.getBlockAt(adjacentPos)
    } else {
        return null
    }
}

fun Block.setLight(level: Int) {
    LightAPI.createLight(location, LightType.BLOCK, level, false)
}

fun Block.deleteLight() {
    LightAPI.deleteLight(location, LightType.BLOCK, false)
}

fun Player.rayTraceEndBlock(distance: Double): Block {
    val tracedLocation = location.direction.clone()
        .normalize()
        .multiply(distance)
    return world.getBlockAt(location.clone().add(tracedLocation).add(0.0, 1.8, 0.0))
}

fun updateLight(location: Location) {
    LightAPI.collectChunks(location, LightType.BLOCK, 15).forEach {
        LightAPI.updateChunk(it, LightType.BLOCK)
    }
}

fun BlockFace.clockWiseNext() = when (this) {
    BlockFace.EAST -> BlockFace.SOUTH
    BlockFace.SOUTH -> BlockFace.WEST
    BlockFace.WEST -> BlockFace.NORTH
    BlockFace.NORTH -> BlockFace.EAST
    else -> error("Unsupported block face: $this")
}

fun BlockFace.counterClockWiseNext() = when (this) {
    BlockFace.EAST -> BlockFace.NORTH
    BlockFace.SOUTH -> BlockFace.EAST
    BlockFace.WEST -> BlockFace.SOUTH
    BlockFace.NORTH -> BlockFace.WEST
    else -> error("Unsupported block face: $this")
}

fun ItemStack.rename(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name)
    meta.lore?.clear()
    meta.isUnbreakable = true
    meta.addItemFlags(*ItemFlag.values())
    setItemMeta(meta)
}
