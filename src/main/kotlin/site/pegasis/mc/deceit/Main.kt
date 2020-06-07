package site.pegasis.mc.deceit

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Lantern
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.pegasis.mc.deceit.combat.CombatListener
import site.pegasis.mc.deceit.objective.ObjectiveManager
import kotlin.experimental.or
import kotlin.random.Random

var debug = true
val tempPigs = arrayListOf<Pig>()
var marking = false

open class Main : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(TPLobby(this), this)
        server.pluginManager.registerEvents(ItemFrameListener(this), this)
        server.pluginManager.registerEvents(TrapDoorListener(this), this)
        server.pluginManager.registerEvents(TransformListener(this), this)
        server.pluginManager.registerEvents(FuseListener(this), this)
        server.pluginManager.registerEvents(NoDropListener(this), this)
        server.pluginManager.registerEvents(ServerStopListener(), this)
        server.pluginManager.registerEvents(CombatListener(this), this)
        server.pluginManager.registerEvents(DisableAttackEntity(), this)
        server.pluginManager.registerEvents(MarkListener(), this)
        server.pluginManager.registerEvents(InteractDistanceListener(), this)
        server.pluginManager.registerEvents(NoArrowPickupListener(), this)
        Game.init(this)
        Environment.init(this)
        FallingBlockManager.init(this)
        FuseSocketManager.init(this)
        FuseManager.init(this)

        val protocolManager = ProtocolLibrary.getProtocolManager()!!
        protocolManager.addPacketListener(object : PacketAdapter(
            this,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.NAMED_ENTITY_SPAWN
        ) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val gp = player.getGP() ?: return
                val entityID = event.packet.integers.read(0)
                val packetType = event.packetType
                if (entityID !in gp.glowingEntityIDs) return

                if (packetType == PacketType.Play.Server.ENTITY_METADATA) {
                    val dataWatchers = event.packet.watchableCollectionModifier.read(0)
                    val watchableObject = dataWatchers.find { it.index == 0 } ?: return

                    var byte = watchableObject.value as Byte
                    byte = byte or 0b01000000
                    watchableObject.setValue(byte)
                } else if (packetType == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                    val dataWatcher = event.packet.dataWatcherModifier.read(0)
                    if (!dataWatcher.hasIndex(0)) return

                    var byte = dataWatcher.getByte(0)
                    byte = byte or 0b01000000
                    dataWatcher.setObject(0, byte)
                }
            }
        })
    }

    override fun onDisable() {
        super.onDisable()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "start-deciet") {
            GlobalScope.launch {
                startGame()
            }
            return true
        } else if (command.name == "light-off") {
            Environment.lightOff(args.firstOrNull() == "full")
            return true
        } else if (command.name == "light-on") {
            Environment.lightOn()
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
                                block.type == Material.JACK_O_LANTERN
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
            }
            return true
        }
        return false
    }

    private suspend fun startGame() {
        GamePlayer.preStart(this)

        ObjectiveManager.hook()
        DoorManager.hook()
        FuseSocketManager.hook()
        FallingBlockManager.hook()
        FuseManager.hook()
        GamePlayer.hook()
        BloodPacks.hook()
        Environment.hook()

        Game.start()
    }
}
