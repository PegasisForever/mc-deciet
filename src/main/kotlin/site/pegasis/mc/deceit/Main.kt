package site.pegasis.mc.deceit

import org.bukkit.plugin.java.JavaPlugin

val debug = true

open class Main : JavaPlugin() {
    override fun onEnable() {
        logger.logInfo("Enabled")
        server.pluginManager.registerEvents(TPLobby(this), this)
    }
}
