package site.pegasis.mc.deceit.combat

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*

class CombatListener(val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        val attacker = event.damager
        val attacked = event.entity
        if (!Game.started || attacked !is Player) return
        val gp = attacked.getGP() ?: return
        if (attacker is Player) {
            if (attacker.location.distanceSquared(attacked.location) > Config.knifeDistance * Config.knifeDistance) {
                event.cancel()
            } else {
                event.damage = Config.knifeDamage
            }
        } else if (event.cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            event.damage = Config.gunDamage
        }

        if (attacked.health - event.finalDamage <= 0) {
            gp.state = PlayerState.DYING
        }
    }

    @EventHandler
    fun onHealthRegen(event: EntityRegainHealthEvent) {
        val player = event.entity
        if (!Game.started || player !is Player) return
        event.cancel()
    }
}
