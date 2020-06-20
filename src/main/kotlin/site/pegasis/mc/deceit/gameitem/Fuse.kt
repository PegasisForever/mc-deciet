package site.pegasis.mc.deceit.gameitem

import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.GamePlayer
import site.pegasis.mc.deceit.Main

class Fuse : GameItem(
    ItemStack(Config.fuseMaterial).apply {
        rename("Fuse")
    }
) {
    override fun onAttach(gp: GamePlayer) {
        this.gp = gp
        Main.registerEvents(this)
    }
}
