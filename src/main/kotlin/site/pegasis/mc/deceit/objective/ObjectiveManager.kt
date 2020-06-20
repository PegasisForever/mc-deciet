package site.pegasis.mc.deceit.objective

import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import site.pegasis.mc.deceit.Game
import site.pegasis.mc.deceit.GameEvent
import site.pegasis.mc.deceit.gameitem.GameItem

interface Objective: Listener {
    fun destroyAndReset()
}

object ObjectiveManager {
    val objectives = arrayListOf<Objective>()

    fun getGameItemPool(size: Int): ArrayList<GameItem> {
        val list = arrayListOf<GameItem>()
        repeat(size) {
            list += GameItem.getRandomObjectiveItem()
        }
        return list
    }

    fun hook() {
        Game.addListener(GameEvent.ON_LEVEL_START) {
            val gameItemPoolSize = Game.level.objAs.size + Game.level.objBs.size + Game.level.objCs.size
            val gameItemPool = getGameItemPool(gameItemPoolSize)

            Game.level.objAs.forEach { (pos, leverPos) ->
                val obj = ObjectiveA(pos, leverPos, gameItemPool.removeAt(0))
                server.pluginManager.registerEvents(obj, this)
                objectives.add(obj)
            }
            Game.level.objBs.forEach { (pos, leverPos, framePos) ->
                val obj = ObjectiveB(pos, leverPos, framePos, gameItemPool.removeAt(0))
                server.pluginManager.registerEvents(obj, this)
                objectives.add(obj)
            }
            Game.level.objCs.forEach { (pos, leverPos, platePos) ->
                val obj = ObjectiveC(pos, leverPos, platePos, gameItemPool.removeAt(0))
                server.pluginManager.registerEvents(obj, this)
                objectives.add(obj)
            }
        }

        Game.addListener(GameEvent.ON_LEVEL_END) {
            objectives.forEach { obj ->
                obj.destroyAndReset()
            }
        }
    }
}
