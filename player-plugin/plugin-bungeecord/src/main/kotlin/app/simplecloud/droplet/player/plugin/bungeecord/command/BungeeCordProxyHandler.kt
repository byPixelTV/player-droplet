package app.simplecloud.droplet.player.plugin.bungeecord.command

import app.simplecloud.droplet.player.plugin.shared.command.ProxyHandler
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.md_5.bungee.api.ProxyServer

class BungeeCordProxyHandler(private val proxyServer: ProxyServer, private val audiences: BungeeAudiences): ProxyHandler {
    override fun getServers(): List<String> {
        return proxyServer.servers.map { it.value.name }
    }

    override fun connectPlayer(playerName: String, serverName: String): Boolean {
        val player = proxyServer.getPlayer(playerName) ?: return false
        val server = proxyServer.getServerInfo(serverName) ?: return false
        if (player.server.info == server) {
            return false
        }
        player.connect(server)
        return true
    }

    override fun disconnectPlayer(playerName: String): Boolean {
        val player = proxyServer.getPlayer(playerName) ?: return false
        player.disconnect("You have been disconnected from the network")
        return true
    }

    override fun sendMessageToPlayer(playerName: String, message: Component): Boolean {
        val player = proxyServer.getPlayer(playerName) ?: return false
        audiences.sender(player).sendMessage(message)
        return true
    }
}