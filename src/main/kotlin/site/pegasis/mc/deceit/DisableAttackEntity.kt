package site.pegasis.mc.deceit

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class DisableAttackEntity : Listener {
    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        val attacked = event.entity
        val attacker = event.damager
        if (Game.started &&
            attacker is Player &&
            attacker.gameMode != GameMode.CREATIVE &&
            attacked !is Player
        ) {
            event.cancel()
        } else if (event.cause == EntityDamageEvent.DamageCause.PROJECTILE && attacked !is Player) {
            event.cancel()
        }
    }
}
