package app.simplecloud.droplet.player.plugin.velocity.connection

import app.simplecloud.pubsub.PubSubListener
import build.buf.gen.simplecloud.droplet.player.v1.CloudPlayerKickEvent
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.util.*

class CloudPlayerKickListener(
    private val proxyServer: ProxyServer,
    private val componentSerializer: GsonComponentSerializer = GsonComponentSerializer.gson(),
) : PubSubListener<CloudPlayerKickEvent> {
    override fun handle(message: CloudPlayerKickEvent) {
        proxyServer.getPlayer(UUID.fromString(message.uniqueId)).ifPresent {
            it.disconnect(componentSerializer.deserialize(message.reason.json))
        }
    }
}