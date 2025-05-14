package app.simplecloud.droplet.player.runtime.service

import app.simplecloud.droplet.player.runtime.connection.PlayerConnectionHandler
import app.simplecloud.droplet.player.runtime.repository.JooqPlayerRepository
import app.simplecloud.droplet.player.shared.rabbitmq.RabbitMqChannelNames
import app.simplecloud.pubsub.PubSubClient
import build.buf.gen.simplecloud.droplet.player.v1.*
import io.grpc.Status
import io.grpc.StatusException
import org.apache.logging.log4j.LogManager
import java.util.*

class PlayerService(
    private val pubSubClient: PubSubClient,
    private val jooqPlayerRepository: JooqPlayerRepository,
    private val playerConnectionHandler: PlayerConnectionHandler
) : PlayerServiceGrpcKt.PlayerServiceCoroutineImplBase() {

    override suspend fun getOfflineCloudPlayerByUniqueId(request: GetCloudPlayerByUniqueIdRequest): GetOfflineCloudPlayerResponse {
        val offlinePlayer = jooqPlayerRepository.findByUniqueId(request.uniqueId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("OfflineCloudPlayer with uniqueId ${request.uniqueId} not found"))


        return GetOfflineCloudPlayerResponse.newBuilder()
            .setOfflineCloudPlayer(offlinePlayer.toConfiguration())
            .build()
    }

    override suspend fun getOfflineCloudPlayerByName(request: GetCloudPlayerByNameRequest): GetOfflineCloudPlayerResponse {
        val offlinePlayer = jooqPlayerRepository.findByName(request.name)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("OfflineCloudPlayer with name ${request.name} not found"))

        return GetOfflineCloudPlayerResponse.newBuilder()
            .setOfflineCloudPlayer(offlinePlayer.toConfiguration())
            .build()
    }

    override suspend fun getCloudPlayerByUniqueId(request: GetCloudPlayerByUniqueIdRequest): GetCloudPlayerResponse {
        val cloudPlayer = jooqPlayerRepository.findByUniqueId(request.uniqueId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} not found"))

        return GetCloudPlayerResponse.newBuilder()
            .setCloudPlayer(cloudPlayer.toCloudPlayerConfiguration())
            .build()
    }

    override suspend fun getCloudPlayerByName(request: GetCloudPlayerByNameRequest): GetCloudPlayerResponse {
        val cloudPlayer = jooqPlayerRepository.findByName(request.name)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with name ${request.name} not found"))

        return GetCloudPlayerResponse.newBuilder()
            .setCloudPlayer(cloudPlayer.toCloudPlayerConfiguration())
            .build()
    }

    override suspend fun getOnlineCloudPlayers(request: GetOnlineCloudPlayersRequest): GetOnlineCloudPlayersResponse {
        return GetOnlineCloudPlayersResponse.newBuilder()
            .addAllOnlineCloudPlayers(jooqPlayerRepository.findAll().filter { it.lastPlayerConnection.online }
                .map { it.toCloudPlayerConfiguration() })
            .build()
    }

    override suspend fun getOnlineCloudPlayerCount(request: GetOnlineCloudPlayerCountRequest): GetOnlineCloudPlayerCountResponse {
        return GetOnlineCloudPlayerCountResponse.newBuilder()
            .setCount(jooqPlayerRepository.count())
            .build()
    }

    override suspend fun getOnlineStatus(request: GetOnlineStatusRequest): GetOnlineStatusResponse {
        val cloudPlayer = jooqPlayerRepository.findByUniqueId(request.uniqueId)
            ?: throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} not found"))

        return GetOnlineStatusResponse.newBuilder()
            .setOnline(cloudPlayer.lastPlayerConnection.online)
            .build()
    }

    override suspend fun loginCloudPlayer(request: CloudPlayerLoginRequest): CloudPlayerLoginResponse {
        val success = playerConnectionHandler.handleLogin(request)

        return CloudPlayerLoginResponse.newBuilder()
            .setSuccess(success)
            .build()
    }

    override suspend fun disconnectCloudPlayer(request: CloudPlayerDisconnectRequest): CloudPlayerDisconnectResponse {
        val success = playerConnectionHandler.handleLogout(request)

        return CloudPlayerDisconnectResponse.newBuilder()
            .setSuccess(success)
            .build()
    }

    override suspend fun kickCloudPlayer(request: CloudPlayerKickRequest): CloudPlayerKickResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.CONNECTION,
                CloudPlayerKickEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
           return CloudPlayerKickResponse.newBuilder().setSuccess(true).build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun connectCloudPlayerToServer(request: ConnectCloudPlayerRequest): ConnectCloudPlayerResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.CONNECTION,
                ConnectCloudPlayerEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return ConnectCloudPlayerResponse.newBuilder().setResult(CloudPlayerConnectResult.SUCCESS).build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun updateCloudPlayerServer(request: UpdateCloudPlayerServerRequest): UpdateCloudPlayerServerResponse {
        if (playerIsOnline(request.uniqueId)) {
            jooqPlayerRepository.updateCurrentServer(UUID.fromString(request.uniqueId), request.serverName)
            return UpdateCloudPlayerServerResponse.newBuilder().setSuccess(true).build()
        } else {
            return UpdateCloudPlayerServerResponse.newBuilder().setSuccess(false).build()
        }
    }

    private suspend fun playerIsOnline(uniqueId: String): Boolean {
        return if (!jooqPlayerRepository.findByUniqueId(uniqueId)!!.lastPlayerConnection.online) {
            LOGGER.warn("CloudPlayer with uniqueId $uniqueId is not online")
            false
        } else {
            true
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(PlayerService::class.java)
    }

}