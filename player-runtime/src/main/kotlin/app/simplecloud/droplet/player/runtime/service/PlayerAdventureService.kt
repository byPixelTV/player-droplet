package app.simplecloud.droplet.player.runtime.service

import app.simplecloud.droplet.player.runtime.repository.JooqPlayerRepository
import app.simplecloud.droplet.player.shared.rabbitmq.RabbitMqChannelNames
import app.simplecloud.pubsub.PubSubClient
import build.buf.gen.simplecloud.droplet.player.v1.*
import io.grpc.Status
import io.grpc.StatusException
import org.apache.logging.log4j.LogManager

class PlayerAdventureService(
    private val pubSubClient: PubSubClient,
    private val onlinePlayerRepository: JooqPlayerRepository,
) : PlayerAdventureServiceGrpcKt.PlayerAdventureServiceCoroutineImplBase() {

    override suspend fun sendMessage(request: SendMessageRequest): SendMessageResponse {
        val cloudPlayer = onlinePlayerRepository.findByUniqueId(request.uniqueId)
        if (cloudPlayer == null) {
            LOGGER.warn("CloudPlayer with uniqueId ${request.uniqueId} is not online")
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }

        pubSubClient.publish(
            RabbitMqChannelNames.ADVENTURE,
            SendMessageEvent.newBuilder().mergeFrom(request.toByteArray()).build()
        )
        LOGGER.info("Sent message to ${cloudPlayer.name}: ${request.message}")

        return SendMessageResponse.newBuilder().build()
    }

    override suspend fun sendActionbar(request: SendActionbarRequest): SendActionbarResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendActionbarEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendActionbarResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }


    override suspend fun sendPlayerListHeaderAndFooter(request: SendPlayerListHeaderAndFooterRequest): SendPlayerListHeaderAndFooterResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendPlayerListHeaderAndFooterEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendPlayerListHeaderAndFooterResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendBossBar(request: SendBossBarRequest): SendBossBarResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendBossBarEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendBossBarResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendBossBarRemove(request: SendBossBarHideRequest): SendBossBarHideResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendBossBarHideEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendBossBarHideResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendClearTitle(request: SendClearTitleRequest): SendClearTitleResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendClearTitleEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendClearTitleResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendOpenBook(request: SendOpenBookRequest): SendOpenBookResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendOpenBookEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendOpenBookResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendPlaySound(request: SendPlaySoundRequest): SendPlaySoundResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendPlaySoundEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendPlaySoundResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendPlaySoundToCoordinates(request: SendPlaySoundToCoordinatesRequest): SendPlaySoundToCoordinatesResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendPlaySoundToCoordinatesEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendPlaySoundToCoordinatesResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendResetTitle(request: SendResetTitleRequest): SendResetTitleResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendResetTitleEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendResetTitleResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendStopSound(request: SendStopSoundRequest): SendStopSoundResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendStopSoundEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )
            return SendStopSoundResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendTitlePartSubTitle(request: SendTitlePartSubTitleRequest): SendTitlePartSubTitleResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendTitlePartSubTitleEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )


            return SendTitlePartSubTitleResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendTitlePartTitle(request: SendTitlePartTitleRequest): SendTitlePartTitleResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendTitlePartTitleEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )

            LOGGER.info("SendTitlePartTitle")

            return SendTitlePartTitleResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    override suspend fun sendTitlePartTimes(request: SendTitlePartTimesRequest): SendTitlePartTimesResponse {
        if (playerIsOnline(request.uniqueId)) {
            pubSubClient.publish(
                RabbitMqChannelNames.ADVENTURE,
                SendTitlePartTimesEvent.newBuilder().mergeFrom(request.toByteArray()).build()
            )

            LOGGER.info("SendTitlePartTimes")

            return SendTitlePartTimesResponse.newBuilder().build()
        } else {
            throw StatusException(Status.NOT_FOUND.withDescription("CloudPlayer with uniqueId ${request.uniqueId} is not online"))
        }
    }

    private suspend fun playerIsOnline(uniqueId: String): Boolean {
        val cloudPlayer = onlinePlayerRepository.findByUniqueId(uniqueId)!!.lastPlayerConnection.online
        return if (!cloudPlayer) {
            LOGGER.warn("CloudPlayer with uniqueId $uniqueId is not online")
            false
        } else {
            true
        }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(PlayerAdventureService::class.java)
    }

}