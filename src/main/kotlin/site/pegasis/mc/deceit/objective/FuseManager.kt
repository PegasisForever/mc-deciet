package site.pegasis.mc.deceit.objective

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.*
import site.pegasis.mc.deceit.gameitem.Fuse

data class FuseEntityBlock(val block: Block, val fallingBlock: ConsistentFallingBlock) : Listener {
    var taken: Boolean = false
        set(value) {
            if (value) {
                fallingBlock.remove()
                FuseManager.availableFuses.remove(this)
                field = value
            }
        }

    init {
        Main.registerEvents(this)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (Game.state != GameState.DARK && Game.state != GameState.RAGE) return
        val player = event.player
        val gp = player.getGP() ?: return

        if (event.rightClicked == fallingBlock.block && !gp.hasFuse) {
            taken = true
            gp.addGameItem(Fuse())
        }
    }
}

object FuseManager {
    val availableFuses = arrayListOf<FuseEntityBlock>()
    lateinit var plugin: JavaPlugin

    fun init(plugin: JavaPlugin) {
        FuseManager.plugin = plugin
    }

    fun hook() {
        val world = Bukkit.getWorld(Config.worldName)!!
        Game.addListener(GameEvent.ON_LEVEL_START) {
            Game.level.fusePositions.forEach { pos ->
                world.getBlockAt(pos).setType(Material.AIR)
                world.getBlockAt(pos.copy(y = pos.y - 1)).setType(Material.AIR)
            }
        }
        Game.addListener(GameEvent.ON_DARK) {
            Game.level.fusePositions.shuffled()
                .take(Game.level.fuseCount)
                .forEach { pos ->
                    world.getBlockAt(pos.copy(y = pos.y - 1))
                        .setType(Config.fuseBaseMaterial)

                    val block = world.getBlockAt(pos)
                    val fallingBlock = FallingBlockManager.add(
                        block.location.clone().apply { x += 0.5; z += 0.5 },
                        Config.fuseMaterial.createBlockData()
                    )
                    // todo add id to glowing list
                    availableFuses += FuseEntityBlock(
                        block,
                        fallingBlock
                    )
                }
        }
        Game.addListener(GameEvent.ON_LEVEL_END) {
            availableFuses.forEach { fuse ->
                fuse.fallingBlock.remove()
            }
            availableFuses.clear()

            val positions = Game.level.fusePositions
            plugin.runDelayed(Config.removeEntityWaitSecond) {
                positions.forEach { pos ->
                    world.getBlockAt(pos).setType(Config.fuseMaterial)
                    world.getBlockAt(pos.copy(y = pos.y - 1))
                        .setType(Config.fuseBaseMaterial)
                }
            }
        }
    }
}
