package app.simplecloud.droplet.player.api

import java.util.*

interface OfflineCloudPlayer {

    /**
     * @returns the uniqueId of the player.
     */
    fun getUniqueId(): UUID

    /**
     * @returns the name of the player.
     */
    fun getName(): String

    /**
     * @returns the display name of the player.
     */
    fun getDisplayName(): String

    /**
     * @returns the first login time of the player.
     */
    fun getFirstLogin(): Long

    /**
     * @returns the last login time of the player.
     */
    fun getLastLogin(): Long

    /**
     * @returns the last connected server name of the player.
     */
    fun getLastConnectedServerName(): String?

    /**
     * @returns the online time of the player.
     */
    fun getOnlineTime(): Long

    /**
     * @returns if the player is online.
     */
    fun isOnline(): Boolean

}