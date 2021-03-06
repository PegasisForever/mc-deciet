package site.pegasis.mc.deceit

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.block.data.Directional
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Pig
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import ru.beykerykt.lightapi.LightAPI
import ru.beykerykt.lightapi.LightType
import site.pegasis.mc.deceit.combat.CombatListener
import site.pegasis.mc.deceit.debug.Debugger
import site.pegasis.mc.deceit.debug.MarkListener
import site.pegasis.mc.deceit.debug.ServerStopListener
import site.pegasis.mc.deceit.environment.*
import site.pegasis.mc.deceit.gameitem.PickupDroppedItemListener
import site.pegasis.mc.deceit.objective.ObjectiveManager
import site.pegasis.mc.deceit.objective.bloodbag.BloodBagManager
import site.pegasis.mc.deceit.objective.fuse.FuseManager
import site.pegasis.mc.deceit.objective.fuse.FuseSocketManager
import site.pegasis.mc.deceit.player.GamePlayerManager
import site.pegasis.mc.deceit.rules.*

var debug = true
val tempPigs = arrayListOf<Pig>()
var marking = false
lateinit var lightPos: Location

open class Main : JavaPlugin(), Listener {
    companion object {
        lateinit var plugin: JavaPlugin
        fun registerEvents(l: Listener) {
            plugin.server.pluginManager.registerEvents(l, plugin)
        }
    }

    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(TPLobby(this), this)
        server.pluginManager.registerEvents(TrapDoorListener(this), this)
        server.pluginManager.registerEvents(NoDropListener(this), this)
        server.pluginManager.registerEvents(ServerStopListener(), this)
        server.pluginManager.registerEvents(CombatListener(this), this)
        server.pluginManager.registerEvents(DisableAttackEntity(), this)
        server.pluginManager.registerEvents(MarkListener(), this)
        server.pluginManager.registerEvents(InteractDistanceListener(), this)
        server.pluginManager.registerEvents(NoPickupListener(), this)
        server.pluginManager.registerEvents(PickupDroppedItemListener(), this)
        Game.init(this)
        GlowingManager.init(this)
        FallingBlockManager.init(this)
        FuseSocketManager.init(this)
        FuseManager.init(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "start-deciet") {
            GlobalScope.launch {
                startGame()
            }
            return true
        } else if (command.name == "light-off") {
            LightManager.lightOff(args.firstOrNull() == "full")
            return true
        } else if (command.name == "light-on") {
            LightManager.lightOn()
            return true
        } else if (command.name == "debug-deceit") {
            when (args.firstOrNull()) {
                "lights" -> {
                    val world = Bukkit.getWorld(Config.worldName)!!
                    val list = arrayListOf<BlockPos>()
                    world.loadedChunks.forEach { chunk ->
                        chunk.forEachBlock { block ->
                            if (block.type == Material.TORCH ||
                                block.type.toString().startsWith("POTTED") ||
                                block.type == Material.LANTERN ||
                                block.type == Material.JACK_O_LANTERN ||
                                (block.type.toString().endsWith("BANNER") && Config.bannerStates.any {
                                    it.first.isMatch(
                                        block.state as Banner
                                    )
                                })
                            ) {
                                list += block.blockPos
                            }
                        }
                    }
                    log(list.joinToString())
                }
                "fuses" -> {
                    val world = Bukkit.getWorld(Config.worldName)!!
                    val list = arrayListOf<BlockPos>()
                    world.loadedChunks.forEach { chunk ->
                        chunk.forEachBlock { block ->
                            if (block.type == Config.fuseMaterial) {
                                list += block.blockPos
                            }
                        }
                    }
                    log(list.joinToString())
                }
                "fuse-sockets" -> {
                    val world = Bukkit.getWorld(Config.worldName)!!
                    val list = arrayListOf<BlockPos>()
                    world.loadedChunks.forEach { chunk ->
                        chunk.forEachBlock { block ->
                            if (block.type == Material.END_PORTAL_FRAME) {
                                list += block.blockPos
                            }
                        }
                    }
                    log(list.joinToString())
                }
                "blood-packs" -> {
                    val world = Bukkit.getWorld(Config.worldName)!!
                    val list = arrayListOf<EntityPos>()
                    world.getEntitiesByClass(ItemFrame::class.java).forEach { itemFrame ->
                        if (itemFrame.item.type == Material.POTION || itemFrame.item.type == Material.GLASS_BOTTLE) {
                            list += itemFrame.entityPos
                        }
                    }
                    log(list.joinToString())
                }
                "anvil" -> {
                    val world = Bukkit.getWorld(Config.worldName)!!
                    val list = arrayListOf<BlockPos>()
                    world.loadedChunks.forEach { chunk ->
                        chunk.forEachBlock { block ->
                            if (block.type == Material.ANVIL) {
                                list += block.blockPos
                            }
                        }
                    }
                    log(list.joinToString())
                }
                "mark" -> {
                    marking = true
                }
                "mark-done" -> {
                    marking = false
                    log(tempPigs.filter { !it.isDead }.map { it.entityPos }.joinToString())
                    tempPigs.forEach { it.remove() }
                    tempPigs.clear()
                }
                "fix1" -> {
                    val world = Bukkit.getWorld(Config.worldName)!!
                    Config.lightSources.forEach { pos ->
                        val block = world.getBlockAt(pos)
                        if (block.type == Material.REDSTONE_TORCH) {
                            block.setType(Material.TORCH)
                        } else if (block.type == Material.CARVED_PUMPKIN) {
                            val facing = (block.blockData as Directional).facing
                            block.setType(Material.JACK_O_LANTERN)
                            block.setBlockData((block.blockData as Directional).apply { setFacing(facing) })
                        }
                    }
                }
                "fix2" -> {
                    val world = Bukkit.getWorld(Config.worldName)!!
                    Config.lightSources.forEach { pos ->
                        val block = world.getBlockAt(pos)
                        if (block.type == Material.AIR) {
                            block.setType(Material.TORCH)
                        }
                    }
                }
                "true" -> debug = true
                "false" -> debug = false
                "cl" -> {
                    lightPos = Bukkit.getPlayer("Pegasis")!!.location
                    LightAPI.createLight(lightPos, LightType.BLOCK, 15, false)
                    LightAPI.collectChunks(lightPos, LightType.BLOCK, 15).forEach {
                        LightAPI.updateChunk(it, LightType.BLOCK)
                    }

                }
                "dl" -> {
                    LightAPI.deleteLight(lightPos, LightType.BLOCK, false)
                    LightAPI.collectChunks(lightPos, LightType.BLOCK, 15).forEach {
                        LightAPI.updateChunk(it, LightType.BLOCK)
                    }
                }
                "dl-all" -> {
                    Game.world.loadedChunks.forEach {
                        it.forEachBlock { block ->
                            LightAPI.deleteLight(block.location, LightType.BLOCK, false)
                        }
                    }
                    LightAPI.collectChunks(lightPos, LightType.BLOCK, 15).forEach {
                        LightAPI.updateChunk(it, LightType.BLOCK)
                    }
                }
                "banner" -> {
                    val block = Debugger.player!!.getTargetBlock(10)
                    if (block?.type?.toString()?.endsWith("BANNER") == true) {
                        val bannerState = block.state as Banner
                        Debugger.msg(block.type.toString())
                        Debugger.msg(bannerState.patterns.joinToString {
                            "${it.color} ${it.pattern}"
                        })
                    }
                }
                "change-banner" -> {
                    val block = Debugger.player!!.getTargetBlock(10)
                    if (block?.type?.toString()?.endsWith("BANNER") == true) {
                        val bannerState = block.state as Banner
                        bannerState.baseColor = DyeColor.WHITE
                        bannerState.patterns = listOf(
                            Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
                            Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER)
                        )
                        bannerState.update()
                    }
                }
            }
            return true
        }
        return false
    }

    private suspend fun startGame() {
        GamePlayerManager.preStart(this)

        ObjectiveManager.hook()
        DoorManager.hook()
        FuseSocketManager.hook()
        FallingBlockManager.hook()
        FuseManager.hook()
        GamePlayerManager.hook()
        BloodBagManager.hook()
        LightManager.hook()
        DroppedItemManager.hook()

        Game.start()
    }
}
