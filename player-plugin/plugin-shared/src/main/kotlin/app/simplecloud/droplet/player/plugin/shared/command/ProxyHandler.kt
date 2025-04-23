package app.simplecloud.droplet.player.plugin.shared.command

import net.kyori.adventure.text.Component

interface ProxyHandler {

    fun getServers(): List<String>

    fun connectPlayer(playerName: String, serverName: String): Boolean

    fun disconnectPlayer(playerName: String): Boolean

    fun sendMessageToPlayer(playerName: String, message: Component): Boolean

}