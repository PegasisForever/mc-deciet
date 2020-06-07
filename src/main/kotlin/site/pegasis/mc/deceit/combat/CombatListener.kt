package site.pegasis.mc.deceit.combat

import org.bukkit.entity.Arrow
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
        if (!Game.started) return
        val isShoot = event.cause == EntityDamageEvent.DamageCause.PROJECTILE
        val attacker = if (isShoot) {
            (((event.damager as? Arrow)?.shooter) as? Player)
        } else {
            event.damager as? Player
        }?.getGP() ?: return
        val attacked = (event.entity as? Player)?.getGP() ?: return

        if (!isShoot && attacker.distanceSquared(attacked) > Config.knifeDistance * Config.knifeDistance) {
            event.cancel()
            return
        }

        if (attacked.state == PlayerState.VOTING) {
            event.cancel()
            attacked.vote(attacker)
        } else {
            event.damage = when {
                attacker.state == PlayerState.TRANSFORMED -> Config.transformedDamage
                isShoot -> Config.gunDamage
                else -> Config.knifeDamage
            }

            if (attacked.player.health - event.finalDamage <= 0) {
                if (attacker.state == PlayerState.TRANSFORMED) {
                    attacked.state = PlayerState.DYING
                } else if (Game.state == GameState.LIGHT || Game.state == GameState.RUN) {
                    attacked.state = PlayerState.VOTING
                } else {
                    attacked.respawn()
                }
                event.cancel()
            }
        }
    }

    @EventHandler
    fun onHealthRegen(event: EntityRegainHealthEvent) {
        if (!Game.started || event.entity !is Player) return
        event.cancel()
    }
}
