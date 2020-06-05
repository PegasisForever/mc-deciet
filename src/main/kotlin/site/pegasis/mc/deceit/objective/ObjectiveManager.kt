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

    fun getGameItemPool(size: Int): ArrayList<ItemStack> {
        val list = arrayListOf<ItemStack>()
        repeat(size) {
            list += ItemStack(Material.CROSSBOW)
//            when(Random.nextInt(3)){
//
//            }
        }
        return list
    }

    fun hook() {
        Game.addListener(GameEvent.ON_LEVEL_START) {
            val gameItemPoolSize = Game.level.objAs.size + Game.level.objBs.size + Game.level.objCs.size
            val gameItemPool = getGameItemPool(gameItemPoolSize)

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
