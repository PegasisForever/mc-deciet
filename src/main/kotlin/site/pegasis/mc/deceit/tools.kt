package site.pegasis.mc.deceit

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.Callable
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

//suspend fun JavaPlugin.delay(seconds: Double): Unit = suspendCoroutine { cont ->
//    object : BukkitRunnable() {
//        override fun run() {
//            cont.resume(Unit)
//        }
//    }.runTaskLater(this, (seconds * 20).toLong())
//}

fun <T> JavaPlugin.inMainThread(action: () -> T): T {
    return Bukkit.getScheduler().callSyncMethod(this) { action() }.get()
}

fun JavaPlugin.consoleCommand(cmd: String) {
    this.server.dispatchCommand(Bukkit.getConsoleSender(), cmd)
}
