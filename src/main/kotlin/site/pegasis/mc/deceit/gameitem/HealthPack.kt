package site.pegasis.mc.deceit.gameitem

import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Config
import site.pegasis.mc.deceit.player.GamePlayer
import site.pegasis.mc.deceit.rename

class HealthPack : GameItem(ItemStack(Config.healthPackMaterial).apply {
    rename("Health Pack")
}) {
    override fun onAttach(gp: GamePlayer, index: Int) {
        super.onAttach(gp, index)
        val maxHealth = gp.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        gp.player.health = (gp.player.health + maxHealth / 2).coerceAtMost(maxHealth)
        modifyItemStack { amount = 0 }
    }
}
