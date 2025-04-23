package app.simplecloud.droplet.player.api.impl

import app.simplecloud.droplet.player.api.OfflineCloudPlayer
import build.buf.gen.simplecloud.droplet.player.v1.OfflineCloudPlayerConfiguration
import java.util.*

open class OfflineCloudPlayerImpl(
    private val configurationWrapper: OfflineCloudPlayerConfiguration
) : OfflineCloudPlayer {

    private val uniqueId = UUID.fromString(configurationWrapper.uniqueId)

    override fun getUniqueId(): UUID {
        return this.uniqueId
    }

    override fun getName(): String {
        return this.configurationWrapper.name
    }

    override fun getDisplayName(): String {
        return this.configurationWrapper.displayName
    }

    override fun getFirstLogin(): Long {
        return this.configurationWrapper.firstLogin
    }

    override fun getLastLogin(): Long {
        return this.configurationWrapper.lastLogin
    }

    override fun getLastConnectedServerName(): String? {
        return this.configurationWrapper.playerConnection.lastServerName
    }

    override fun getOnlineTime(): Long {
        return this.configurationWrapper.onlineTime
    }

    override fun isOnline(): Boolean {
        return this.configurationWrapper.playerConnection.online
    }

}