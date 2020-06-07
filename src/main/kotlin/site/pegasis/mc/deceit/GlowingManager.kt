package site.pegasis.mc.deceit

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import kotlin.experimental.or

object GlowingManager {
    private val globalGlowingID = hashSetOf<Int>()

    fun init(plugin: JavaPlugin) {
        val protocolManager = ProtocolLibrary.getProtocolManager()!!
        protocolManager.addPacketListener(object : PacketAdapter(
            plugin,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.NAMED_ENTITY_SPAWN
        ) {
            override fun onPacketSending(event: PacketEvent) {
                val entityID = event.packet.integers.read(0)
                if (entityID in globalGlowingID) {
                    event.makeEntityGlow()
                } else {
                    val gp = event.player.getGP() ?: return
                    if (entityID in gp.glowingEntityIDs){
                        event.makeEntityGlow()
                    }
                }
            }
        })
    }

    fun addGlowing(entity: Entity) {
        globalGlowingID += entity.entityId
    }

    private fun PacketEvent.makeEntityGlow() {
        if (packetType == PacketType.Play.Server.ENTITY_METADATA) {
            val dataWatchers = packet.watchableCollectionModifier.read(0)
            val watchableObject = dataWatchers.find { it.index == 0 } ?: return

            var byte = watchableObject.value as Byte
            byte = byte or 0b01000000
            watchableObject.setValue(byte)
        } else if (packetType == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            val dataWatcher = packet.dataWatcherModifier.read(0)
            if (!dataWatcher.hasIndex(0)) return

            var byte = dataWatcher.getByte(0)
            byte = byte or 0b01000000
            dataWatcher.setObject(0, byte)
        }
    }
}
