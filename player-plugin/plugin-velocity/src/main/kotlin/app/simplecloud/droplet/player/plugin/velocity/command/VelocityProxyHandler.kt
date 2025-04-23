package app.simplecloud.droplet.player.plugin.velocity.command

import app.simplecloud.droplet.player.plugin.shared.command.ProxyHandler
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component

class VelocityProxyHandler(private val proxyServer: ProxyServer) : ProxyHandler {
    override fun getServers(): List<String> {
        return proxyServer.allServers.map { it.serverInfo.name }
    }

    override fun connectPlayer(playerName: String, serverName: String): Boolean {
        val player = proxyServer.getPlayer(playerName).orElse(null) ?: return false
        val server = proxyServer.getServer(serverName).orElse(null) ?: return false
        if (player.currentServer == server) {
            return false
        }
        player.createConnectionRequest(server).fireAndForget()
        return true
    }

    override fun disconnectPlayer(playerName: String): Boolean {
        val player = proxyServer.getPlayer(playerName).orElse(null) ?: return false
        player.disconnect(Component.text("You have been disconnected from the network"))
        return true
    }

    override fun sendMessageToPlayer(playerName: String, message: Component): Boolean {
       val player = proxyServer.getPlayer(playerName).orElse(null) ?: return false
        player.sendMessage(message)
        return true
    }


}