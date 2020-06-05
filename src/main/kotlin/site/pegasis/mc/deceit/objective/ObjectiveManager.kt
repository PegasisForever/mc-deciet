package site.pegasis.mc.deceit.objective

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent
import site.pegasis.mc.deceit.log

interface Objective {
    fun destroy()
}

object ObjectiveManager {
    val objectives = arrayListOf<Objective>()

    fun hook() {
        Game.addListener(GameEvent.ON_LEVEL_START) {
            val gameItemPoolCount = Game.level.objAs.size + Game.level.objBs.size + Game.level.objCs.size
            val gameItemPool = arrayListOf<ItemStack>()
            repeat(gameItemPoolCount) {
                gameItemPool += ItemStack(Material.CROSSBOW)
            }
            gameItemPool.shuffle()

            Game.level.objAs.forEach { (pos, leverPos) ->
                val obj = ObjectiveA(pos, leverPos, gameItemPool.removeAt(0), this)
                server.pluginManager.registerEvents(obj, this)
                objectives.add(obj)
            }
            Game.level.objBs.forEach { (pos, leverPos, framePos) ->
                val obj = ObjectiveB(pos, leverPos, framePos, gameItemPool.removeAt(0), this)
                server.pluginManager.registerEvents(obj, this)
                objectives.add(obj)
            }
            Game.level.objCs.forEach { (pos, leverPos, platePos) ->
                val obj = ObjectiveC(pos, leverPos, platePos, gameItemPool.removeAt(0), this)
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
