package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

val debug = true

val torchBlocks = arrayListOf<Block>()
val potBlocks = arrayListOf<Pair<Block, Material>>()

open class Main : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(TPLobby(this), this)
        server.pluginManager.registerEvents(ItemFrameListener(this), this)
        server.pluginManager.registerEvents(TransformListener(this), this)
        server.pluginManager.registerEvents(FuseListener(this), this)
        server.pluginManager.registerEvents(NoDropListener(this), this)
        GameState.init(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "start-deciet") {
            GlobalScope.launch {
                startGame()
            }
            return true
        } else if (command.name == "light-off") {
            torchBlocks.clear()
            val world = Bukkit.getWorld(Config.worldName)!!
            world.loadedChunks.forEach { chunk ->
                chunk.forEachBlock { block ->
                    if (block.type == Material.TORCH) {
                        if (Random.nextInt(10) <= 6) {
                            block.setType(Material.REDSTONE_TORCH)
                        } else {
                            block.setType(Material.AIR)
                        }
                        torchBlocks.add(block)
                    } else if (block.type.toString().startsWith("POTTED")) {
                        potBlocks += (block to block.type)
                        if (Random.nextBoolean()){
                            block.setType(Material.POTTED_DEAD_BUSH)
                        }else{
                            block.setType(Material.POTTED_WITHER_ROSE)
                        }
                    }
                }
            }
            return true
        } else if (command.name == "light-on") {
            torchBlocks.forEach { block ->
                block.setType(Material.TORCH)
            }
            potBlocks.forEach { (block, originalType) ->
                block.setType(originalType)
            }
            return true
        }
        return false
    }

    private suspend fun startGame() {
        GamePlayer.preStart(this)

        GamePlayer.hook()
        BloodPacks.hook()
        Environment.hook()

        GameState.start()
    }
}
