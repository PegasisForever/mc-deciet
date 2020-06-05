package site.pegasis.mc.deceit.objective

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent

interface Objective {
    fun destroy()
}

object ObjectiveManager {
    val objectives = arrayListOf<Objective>()

    fun hook() {
        Game.addListener(GameEvent.ON_LEVEL_START) {
            val gameItemPoolCount = Game.level.objAs.size
            val gameItemPool = arrayListOf<ItemStack>()
            repeat(gameItemPoolCount) {
                gameItemPool += ItemStack(Material.CROSSBOW)
            }
            gameItemPool.shuffle()

            Game.level.objAs.forEach { (objAPos, leverPos) ->
                val obj = ObjectiveA(objAPos, leverPos, gameItemPool.removeAt(0), this)
                server.pluginManager.registerEvents(obj, this)
                objectives.add(obj)
            }
        }

        Game.addListener(GameEvent.ON_LEVEL_END) {
            objectives.forEach { obj ->
                obj.destroy()
            }
        }
    }
}
