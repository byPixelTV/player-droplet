package app.simplecloud.droplet.player.runtime.connection

import app.simplecloud.droplet.player.runtime.entity.OfflinePlayerEntity
import app.simplecloud.droplet.player.runtime.entity.PlayerConnectionEntity
import app.simplecloud.droplet.player.runtime.repository.JooqPlayerRepository
import build.buf.gen.simplecloud.droplet.player.v1.*
import org.apache.logging.log4j.LogManager

class PlayerConnectionHandler(
    private val jooqPlayerRepository: JooqPlayerRepository
) {

    suspend fun handleLogin(request: CloudPlayerLoginRequest): Boolean {
        LOGGER.info("Player {} is logging in...", request.uniqueId)

        createPlayer(request)
        return true
    }

    private suspend fun createPlayer(request: CloudPlayerLoginRequest) {
        val offlinePlayer = jooqPlayerRepository.findByUniqueId(request.uniqueId) ?: createOfflinePlayer(request)
        jooqPlayerRepository.save(offlinePlayer.copy(lastLogin = System.currentTimeMillis(), lastPlayerConnection = offlinePlayer.lastPlayerConnection.copy(online = true, clientLanguage = request.playerConnection.clientLanguage)))
    }

    suspend fun handleLogout(request: CloudPlayerDisconnectRequest): Boolean {
        val onlinePlayer = jooqPlayerRepository.findByUniqueId(request.uniqueId)
        if (onlinePlayer == null) {
            LOGGER.warn("Player {} is not logged in", request.uniqueId)
            return false
        }

        val sessionTime = System.currentTimeMillis() - onlinePlayer.lastLogin
        val offlinePlayerEntity = OfflinePlayerEntity(
            onlinePlayer.uniqueId,
            onlinePlayer.name,
            onlinePlayer.displayName,
            onlinePlayer.firstLogin,
            System.currentTimeMillis(),
            onlinePlayer.onlineTime + sessionTime,
            onlinePlayer.lastPlayerConnection.copy(online = false)
        )

        jooqPlayerRepository.save(offlinePlayerEntity)

        LOGGER.info("Player {} logged out", onlinePlayer.name)
        return true
    }

     private suspend fun createOfflinePlayer(request: CloudPlayerLoginRequest): OfflinePlayerEntity {
        val offlinePlayerEntity = OfflinePlayerEntity(
            request.uniqueId,
            request.name,
            request.name,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            0,
            PlayerConnectionEntity.fromConfiguration(request.playerConnection)
        )

        jooqPlayerRepository.save(offlinePlayerEntity)
        return offlinePlayerEntity
    }

    companion object {
        private val LOGGER = LogManager.getLogger(PlayerConnectionHandler::class.java)
    }

}