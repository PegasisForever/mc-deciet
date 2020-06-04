package site.pegasis.mc.deceit

import org.bukkit.Location
import org.bukkit.Material

object Config {
    val lobbyLocation = Location(null, 731.0, 66.0, 28351.0)
    val originalSkinOverride = mapOf("Pegasis" to "yEco")
    val infectedSkin = "GGsf2" //https://namemc.com/skin/f9b5ba2c8cd83d55
    val worldName = "world"
    val transformDuration = 5 //Seconds
    val bloodPackRestoreTime = 2 //Seconds
    val transformMaterial = Material.MUSIC_DISC_11
    val fuseMaterial = Material.YELLOW_STAINED_GLASS_PANE
}
