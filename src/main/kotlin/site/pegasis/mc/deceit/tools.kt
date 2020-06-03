package site.pegasis.mc.deceit

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.logging.Level
import java.util.logging.Logger

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
