package app.simplecloud.droplet.player.plugin.velocity

import app.simplecloud.droplet.player.api.PlayerApiSingleton
import app.simplecloud.droplet.player.plugin.shared.OnlinePlayerChecker
import app.simplecloud.droplet.player.plugin.shared.command.CloudSender
import app.simplecloud.droplet.player.plugin.shared.command.PlayerCommand
import app.simplecloud.droplet.player.plugin.shared.config.MessageConfig
import app.simplecloud.droplet.player.plugin.shared.config.YamlConfig
import app.simplecloud.droplet.player.plugin.shared.proxy.PlayerProxyApi
import app.simplecloud.droplet.player.plugin.velocity.command.VelocityProxyHandler
import app.simplecloud.droplet.player.plugin.velocity.command.VelocitySender
import app.simplecloud.droplet.player.plugin.velocity.connection.CloudPlayerConnectListener
import app.simplecloud.droplet.player.plugin.velocity.connection.CloudPlayerKickListener
import app.simplecloud.droplet.player.plugin.velocity.listener.PlayerConnectionListener
import app.simplecloud.droplet.player.plugin.velocity.listener.PlayerDisconnectListener
import app.simplecloud.droplet.player.shared.rabbitmq.RabbitMqChannelNames
import build.buf.gen.simplecloud.droplet.player.v1.CloudPlayerKickEvent
import build.buf.gen.simplecloud.droplet.player.v1.ConnectCloudPlayerEvent
import com.google.inject.Inject
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
import java.nio.file.Path
import java.util.*
import kotlin.io.path.pathString

class PlayerVelocityPlugin @Inject constructor(
    private val proxyServer: ProxyServer,
    @DataDirectory val dataDirectory: Path,
    private val pluginContainer: PluginContainer
) {

    private val playerApi = PlayerProxyApi(
        OnlinePlayerChecker { proxyServer.getPlayer(UUID.fromString(it)).isPresent }
    )

    private lateinit var commandManager: VelocityCommandManager<CloudSender>


    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        PlayerApiSingleton.init(playerApi)

        playerApi.registerPubSubListener(
            RabbitMqChannelNames.CONNECTION,
                CloudPlayerKickEvent::class.java, CloudPlayerKickListener((proxyServer)
        ))
        playerApi.registerPubSubListener(
            RabbitMqChannelNames.CONNECTION,
                ConnectCloudPlayerEvent::class.java, CloudPlayerConnectListener((proxyServer)
        ))


        val yamlConfig: YamlConfig = YamlConfig(dataDirectory.pathString)
        val messageConfiguration = yamlConfig.load<MessageConfig>("messages")!!
        yamlConfig.save("messages", messageConfiguration)


        val executionCoordinator = ExecutionCoordinator.simpleCoordinator<CloudSender>()

        val senderMapper = SenderMapper.create<CommandSource, CloudSender>(
            { commandSender -> VelocitySender(commandSender) },
            { cloudSender -> (cloudSender as VelocitySender).getCommandSource() }
        )

        commandManager = VelocityCommandManager(
            pluginContainer,
            proxyServer,
            executionCoordinator,
            senderMapper
        )

        val proxyHandler = VelocityProxyHandler(proxyServer)

        val cloudCommandHandler = PlayerCommand(commandManager, messageConfiguration, proxyHandler)
        cloudCommandHandler.createPlayerCommand()

        proxyServer.eventManager.register(this, PlayerConnectionListener(playerApi, proxyServer, this))
        proxyServer.eventManager.register(this, PlayerDisconnectListener(playerApi))
    }

}