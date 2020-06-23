package site.pegasis.mc.deceit.gameitem

import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.rename

class HealthPack :GameItem(ItemStack(Config.healthPackMaterial).apply {
    rename("Health Pack")
})
