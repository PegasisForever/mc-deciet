package site.pegasis.mc.deceit.player

import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import site.pegasis.mc.deceit.player.GamePlayerEffectFlag.*

enum class GamePlayerEffectFlag {
    TRANSFORMED,
    IN_LIGHT, // when transformed under torch or camera
    TRACKED,
}

fun GamePlayerEffectFlag.applyTo(player: Player) {
    when (this) {
        TRANSFORMED -> player.addPotionEffect(
            PotionEffect(
                PotionEffectType.SPEED,
                10000000,
                1,
                false,
                false,
                false
            )
        )
        IN_LIGHT -> {
            player.addPotionEffect(
                PotionEffect(
                    PotionEffectType.GLOWING,
                    10000000,
                    0,
                    false,
                    false,
                    true
                )
            )
        }
        TRACKED -> player.addPotionEffect(
            PotionEffect(
                PotionEffectType.GLOWING,
                10000000,
                0,
                false,
                false,
                true
            )
        )
    }
}

fun GamePlayerEffectFlag.removeFrom(player: Player){
    when(this){
        TRANSFORMED -> player.removePotionEffect(PotionEffectType.SPEED)
        IN_LIGHT -> {
            player.removePotionEffect(PotionEffectType.GLOWING)
        }
        TRACKED -> player.removePotionEffect(PotionEffectType.GLOWING)
    }
}
