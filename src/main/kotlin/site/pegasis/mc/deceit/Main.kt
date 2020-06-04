package site.pegasis.mc.deceit

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Cow
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.experimental.or


val debug = true

open class Main : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(TPLobby(this), this)
        server.pluginManager.registerEvents(ItemFrameListener(this), this)
        server.pluginManager.registerEvents(TransformListener(this), this)
        server.pluginManager.registerEvents(FuseListener(this), this)
        server.pluginManager.registerEvents(NoDropListener(this), this)
        server.pluginManager.registerEvents(ServerStopListener(), this)
        GameState.init(this)
        Environment.init(this)
        FallingBlockManager.init(this)

        val protocolManager = ProtocolLibrary.getProtocolManager()!!
        protocolManager.addPacketListener(object : PacketAdapter(
            this,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.NAMED_ENTITY_SPAWN
        ) {
            override fun onPacketSending(event: PacketEvent) {
                return
                val player = event.player
                val entityID = event.packet.integers.read(0)
                val packetType = event.packetType
                if (player.name != "Pegasis") return
                val world = Bukkit.getWorld(Config.worldName)!!
                val cowIds=world.getEntitiesByClass(Cow::class.java).map { it.entityId }
                if (entityID !in cowIds) return

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
            Environment.lightOff()
            return true
        } else if (command.name == "light-on") {
            Environment.lightOn()
            return true
        }
        return false
    }

    private suspend fun startGame() {
        GamePlayer.preStart(this)

        FallingBlockManager.hook()
        FuseManager.hook()
        GamePlayer.hook()
        BloodPacks.hook()
        Environment.hook()

        GameState.start()
    }
}
