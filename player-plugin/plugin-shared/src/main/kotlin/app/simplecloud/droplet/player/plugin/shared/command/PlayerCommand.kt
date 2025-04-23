package app.simplecloud.droplet.player.plugin.shared.command

import app.simplecloud.droplet.player.api.PlayerApi
import app.simplecloud.droplet.player.plugin.shared.config.MessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.incendo.cloud.CommandManager
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.permission.Permission
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class PlayerCommand<C : CloudSender>(
    private val commandManager: CommandManager<C>,
    private val messageConfig: MessageConfig,
    private val proxyHandler: ProxyHandler
) {

    private val playerApi = PlayerApi.createFutureApi()

    fun createPlayerCommand() {
        commandManager.command(
            commandManager.commandBuilder("players")
                .handler { context: CommandContext<C> ->

                    context.sender().sendMessage(
                        MiniMessage.miniMessage().deserialize(messageConfig.playerHelpTitle)
                    )
                    context.sender().sendMessage(
                        MiniMessage.miniMessage().deserialize(messageConfig.playerInfoCommand)
                    )
                    context.sender().sendMessage(
                        MiniMessage.miniMessage().deserialize(messageConfig.playerInfoServerCommand)
                    )
                    context.sender().sendMessage(
                        MiniMessage.miniMessage().deserialize(messageConfig.playerSendCommand)
                    )
                    context.sender().sendMessage(
                        MiniMessage.miniMessage().deserialize(messageConfig.playerMessageCommand)
                    )

                }
                .permission(Permission.permission("simplecloud.command.player"))
                .build()
        )


        registerInfoUserCommand()
        registerInfoServerCommand()

        registerPlayerSendCommand()
        registerSendMessageCommand()

    }

    private fun registerInfoUserCommand() {
        commandManager.command(
            commandManager.commandBuilder("players")
                .literal("info")
                .literal("user")
                .required(
                    "user",
                    stringParser(),
                    SuggestionProvider { _, _ ->
                        playerApi.getOnlinePlayers().thenApply { groups ->
                            groups.map { group -> Suggestion.suggestion(group.getDisplayName()) }
                        }
                    }
                )
                .handler { context: CommandContext<C> ->
                    val user = context.get<String>("user")

                    playerApi.getOfflinePlayer(user.lowercase()).thenAccept { player ->
                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.userInfoTitle,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "user",
                                        Component.text(player.getDisplayName())
                                    ),
                                    Placeholder.component(
                                        "online",
                                        MiniMessage.miniMessage()
                                            .deserialize(if (player.isOnline()) "<color:#a3e635>● Online" else "<color:#dc2626>● Offline")
                                    )
                                )
                            )
                        )

                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.userInfoName,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "name",
                                        Component.text(player.getDisplayName())
                                    )
                                )
                            )
                        )

                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.userInfoId,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "id",
                                        Component.text(player.getUniqueId().toString())
                                    )
                                )
                            )
                        )

                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.userInfoServer,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "server",
                                        Component.text(player.getLastConnectedServerName() ?: "none")
                                    )
                                )
                            )
                        )


                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.userInfoFirstLogin,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "firstlogin",
                                        Component.text(formatTimestampToGerman(player.getFirstLogin()))
                                    )
                                )
                            )
                        )

                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.userInfoLastLogin,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "lastlogin",
                                        Component.text(formatTimestampToGerman(player.getLastLogin()))
                                    )
                                )
                            )
                        )

                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.userOnlinetime,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "onlinetime",
                                        Component.text(formatMillisToHoursAndMinutes(player.getOnlineTime()))
                                    )
                                )
                            )
                        )

                    }
                }
                .permission(Permission.permission("simplecloud.command.player.info.user"))
                .build()
        )
    }

    fun registerInfoServerCommand() {
        commandManager.command(
            commandManager.commandBuilder("players")
                .literal("info")
                .literal("server")
                .optional(
                    "server",
                    stringParser(),
                    BlockingSuggestionProvider { _, _ ->
                        proxyHandler.getServers().map { servers ->
                            Suggestion.suggestion(servers)
                        }
                    }
                )
                .handler { context: CommandContext<C> ->
                    val server = context.getOrDefault("server", null as String?)
                    if (server == null) {
                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(messageConfig.serversInfoTitle)
                        )
                        proxyHandler.getServers().forEach { server ->
                            val playerList = mutableListOf<String>()
                            playerApi.getOnlinePlayers().thenApply { players ->
                                players.filter { it.getConnectedServerName() == server }
                                    .forEach { player -> playerList.add(player.getDisplayName()) }


                                context.sender().sendMessage(
                                    MiniMessage.miniMessage().deserialize(
                                        messageConfig.serversInfoServer,
                                        TagResolver.resolver(
                                            Placeholder.component(
                                                "server",
                                                Component.text(server)
                                            ),
                                            Placeholder.component(
                                                "playercount",
                                                Component.text(playerList.size.toString())
                                            ),
                                            Placeholder.component(
                                                "players",
                                                Component.text(formatPlayersToMultilineString(playerList))
                                            )
                                        )
                                    )
                                )
                            }.exceptionally {
                                it.printStackTrace()
                                return@exceptionally
                            }

                        }


                    } else {
                        playerApi.getOnlinePlayers().thenApply { players ->
                            context.sender().sendMessage(
                                MiniMessage.miniMessage().deserialize(
                                    messageConfig.serverInfoTitle, Placeholder.component(
                                        "server",
                                        Component.text(server)
                                    )
                                )
                            )
                            players.filter { it.getConnectedServerName() == server }.forEach { player ->
                                context.sender().sendMessage(
                                    MiniMessage.miniMessage().deserialize(
                                        "    <color:#a3a3a3>${player.getDisplayName()}"
                                    )
                                )
                            }
                        }.exceptionally({
                            it.printStackTrace()
                            return@exceptionally
                        })
                    }

                }
            .permission(Permission.permission("simplecloud.command.player.info.server"))
        )
    }

    private fun registerPlayerSendCommand() {
        commandManager.command(
            commandManager.commandBuilder("players")
                .literal("send")
                .required(
                    "user",
                    stringParser(),
                    SuggestionProvider { _, _ ->
                        playerApi.getOnlinePlayers().thenApply { players ->
                            players.map { player -> Suggestion.suggestion(player.getDisplayName()) }
                        }
                    }
                )
                .required(
                    "server",
                    stringParser(),
                    BlockingSuggestionProvider { _, _ ->
                        proxyHandler.getServers().map { servers ->
                            Suggestion.suggestion(servers)
                        }
                    }
                )
                .handler { context: CommandContext<C> ->
                    println("send player")
                    val user = context.get<String>("user")
                    val server = context.get<String>("server")

                    if (proxyHandler.connectPlayer(user, server)) {
                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.sendPlayer,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "user",
                                        Component.text(user)
                                    ),
                                    Placeholder.component(
                                        "server",
                                        Component.text(server)
                                    )
                                )
                            )
                        )
                    } else {
                        context.sender().sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                messageConfig.sendPlayerFailed,
                                TagResolver.resolver(
                                    Placeholder.component(
                                        "user",
                                        Component.text(user)
                                    ),
                                    Placeholder.component(
                                        "server",
                                        Component.text(server)
                                    )
                                )
                            )
                        )
                    }

                }
            .permission(Permission.permission("simplecloud.command.player.send"))
        )
    }

    private fun registerSendMessageCommand() {
        commandManager.command(
            commandManager.commandBuilder("players")
                .literal("message")
                .required(
                    "user",
                    stringParser(),
                    SuggestionProvider { _, _ ->
                        playerApi.getOnlinePlayers().thenApply { players ->
                            players.map { player -> Suggestion.suggestion(player.getDisplayName()) }
                        }
                    }
                )
                .required(
                    "message",
                    greedyStringParser()
                )
                .handler { context: CommandContext<C> ->
                    val user = context.get<String>("user")
                    val message = context.get<String>("message")
                    proxyHandler.sendMessageToPlayer(user, MiniMessage.miniMessage().deserialize(message))
                    context.sender().sendMessage(
                        MiniMessage.miniMessage().deserialize(
                            messageConfig.sendMessage,
                            TagResolver.resolver(
                                Placeholder.component(
                                    "user",
                                    Component.text(user)
                                ),
                                Placeholder.component(
                                    "message",
                                    MiniMessage.miniMessage().deserialize(message)
                                )
                            )
                        )
                    )
                }
            .permission(Permission.permission("simplecloud.command.player.message"))
        )
    }


    private fun formatTimestampToGerman(timestamp: Long): String {
        val dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.GERMAN)
        return dateTime.format(formatter)
    }

    private fun formatMillisToHoursAndMinutes(millis: Long): String {
        val duration = Duration.ofMillis(millis)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return String.format("%02d:%02d", hours, minutes)
    }

    private fun formatPlayersToMultilineString(players: List<String>): String {
        return players.joinToString(separator = "\n")
    }

}