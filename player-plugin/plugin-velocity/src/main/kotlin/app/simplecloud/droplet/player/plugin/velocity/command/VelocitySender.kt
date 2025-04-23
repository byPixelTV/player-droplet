package app.simplecloud.droplet.player.plugin.velocity.command

import app.simplecloud.droplet.player.plugin.shared.command.CloudSender
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

/**
 * @author Fynn Bauer in 2024
 */
class VelocitySender(private val commandSource: CommandSource) : CloudSender {

    fun getCommandSource(): CommandSource {
        return commandSource
    }

    override fun sendMessage(message: Component) {
        commandSource.sendMessage(message)
    }
}