package app.simplecloud.droplet.player.plugin.shared.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MessageConfig(

    val playerHelpTitle: String = "<color:#38bdf8><bold>⚡</bold></color> <color:#ffffff>Commands of Player Command",
    val playerInfoCommand: String = "   <color:#a3a3a3>/players info user <user>",
    val playerInfoServerCommand: String = "   <color:#a3a3a3>/players info server [server]",
    val playerSendCommand: String = "   <color:#a3a3a3>/players send <user> <server>",
    val playerMessageCommand: String = "   <color:#a3a3a3>/players message <user> <message>",

    val userInfoTitle: String = "<color:#38bdf8><bold>⚡</bold></color> <color:#ffffff>Information of user <user> <online>",
    val userInfoName: String = "   <color:#a3a3a3>Name: <color:#38bdf8><name>",
    val userInfoId: String = "   <color:#a3a3a3>Unique Id: <color:#38bdf8><id>",
    val userInfoServer: String = "   <color:#a3a3a3>Last known server: <color:#38bdf8><server>",
    val userInfoFirstLogin: String = "   <color:#a3a3a3>First login: <color:#38bdf8><firstlogin>",
    val userInfoLastLogin: String = "   <color:#a3a3a3>Last login: <color:#38bdf8><lastlogin>",
    val userOnlinetime: String = "   <color:#a3a3a3>Onlinetime: <color:#38bdf8><onlinetime>",

    val serversInfoTitle: String = "<color:#38bdf8><bold>⚡</bold></color> <color:#ffffff>Servers",
    val serversInfoServer: String = "    <hover:show_text:'<players>'><color:#a3a3a3><server> <color:#a3e635>● <playercount> Online</hover>",

    val serverInfoTitle: String = "<color:#38bdf8><bold>⚡</bold></color> <color:#ffffff>Information of server <server>",

    val sendPlayer: String = "<color:#38bdf8><bold>⚡</bold></color> <color:#ffffff>Sent player <user> to server <server>",
    val sendPlayerFailed: String = "<color:#38bdf8><bold>⚡</bold></color> <color:#ffffff>Failed to send player <user> to server <server>",

    val sendMessage: String = "<color:#38bdf8><bold>⚡</bold></color> <color:#ffffff>Message sent to <user>",

)