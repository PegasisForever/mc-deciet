package site.pegasis.mc.deceit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun JavaPlugin.log(msg: Any) {
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

fun Player.getUnderBlock(): Block {
    var deltaY = 0.0
    while (deltaY > -5) {
        val block = world.getBlockAt(location.add(0.0, deltaY, 0.0))
        if (block.type.isSolid) return block
        deltaY--
    }
    return world.getBlockAt(location)
}
