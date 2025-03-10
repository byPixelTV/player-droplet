package app.simplecloud.droplet.player.runtime

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import app.simplecloud.droplet.api.auth.AuthSecretInterceptor
import app.simplecloud.droplet.player.runtime.connection.PlayerConnectionHandler
import app.simplecloud.droplet.player.runtime.database.DatabaseFactory
import app.simplecloud.droplet.player.runtime.launcher.AuthType
import app.simplecloud.droplet.player.runtime.launcher.PlayerDropletStartCommand
import app.simplecloud.droplet.player.runtime.repository.JooqPlayerRepository
import app.simplecloud.droplet.player.runtime.service.PlayerAdventureService
import app.simplecloud.droplet.player.runtime.service.PlayerService
import app.simplecloud.pubsub.PubSubClient
import app.simplecloud.pubsub.PubSubService
import build.buf.gen.simplecloud.controller.v1.ControllerDropletServiceGrpcKt
import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.logging.log4j.LogManager
import kotlin.concurrent.thread

class PlayerRuntime(
    private val startCommand: PlayerDropletStartCommand
) {

    private val logger = LogManager.getLogger(PlayerRuntime::class.java)

    private val interceptor = if (startCommand.authType == AuthType.AUTH_SERVER) {
        AuthSecretInterceptor(startCommand.controllerHost, startCommand.controllerAuthPort)
    } else {
        StandaloneAuthSecretInterceptor(startCommand.authSecret)
    }

    private val database = DatabaseFactory.createDatabase(startCommand.databaseUrl)
    private val jooqPlayerRepository = JooqPlayerRepository(database)
    private val authCallCredentials = AuthCallCredentials(startCommand.authSecret)
    private val pubSubClient = PubSubClient(startCommand.pubSubGrpcHost, startCommand.pubSubGrpcPort, authCallCredentials)
    private val playerConnectionHandler = PlayerConnectionHandler(jooqPlayerRepository)

    private val controllerChannel =
        ManagedChannelBuilder.forAddress(startCommand.controllerHost, startCommand.controllerPort)
            .usePlaintext()
            .build()
    val controllerDropletStub =
        ControllerDropletServiceGrpcKt.ControllerDropletServiceCoroutineStub(controllerChannel)
            .withCallCredentials(authCallCredentials)

    private val server = createGrpcServerFromEnv()

    private val pubSubServer = createPubSubGrpcServerFromEnv()

    suspend fun start() {
        logger.info("Starting Player server...")
        setupDatabase()
        startGrpcServer()
        attach()
        startPubSubGrpcServer()

        suspendCancellableCoroutine<Unit> { continuation ->
            Runtime.getRuntime().addShutdownHook(Thread {
                server.shutdown()
                continuation.resume(Unit) { cause, _, _ ->
                    logger.info("Server shutdown due to: $cause")
                }
            })
        }
    }

    private fun attach() {
        if (startCommand.authType != AuthType.AUTH_SERVER) {
            return
        }

        logger.info("Attaching to controller...")
        val attacher =
            Attacher(startCommand, controllerChannel, controllerDropletStub)
        attacher.enforceAttach()
    }

    private fun setupDatabase() {
        logger.info("Setting up database...")
        database.setup()
    }

    private fun startGrpcServer() {
        logger.info("Starting gRPC server on ${startCommand.grpcHost} with port ${startCommand.grpcPort}")
        thread {
            server.start()
            server.awaitTermination()
        }
    }

    private fun startPubSubGrpcServer() {
        logger.info("Starting PubSub gRPC server on ${startCommand.pubSubGrpcHost} with port ${startCommand.pubSubGrpcPort}")
        thread {
            pubSubServer.start()
            pubSubServer.awaitTermination()
        }
    }

    private fun createGrpcServerFromEnv(): Server {
        val port = startCommand.grpcPort
        return ServerBuilder.forPort(port)
            .addService(
                PlayerService(
                    pubSubClient,
                    jooqPlayerRepository,
                    playerConnectionHandler
                )
            )
            .addService(PlayerAdventureService(pubSubClient, jooqPlayerRepository))
            .addService(PubSubService())
            .intercept(interceptor)
            .build()
    }

    private fun createPubSubGrpcServerFromEnv(): Server {
        val port = startCommand.pubSubGrpcPort
        return ServerBuilder.forPort(port)
            .addService(PubSubService())
            .intercept(interceptor)
            .build()
    }

}