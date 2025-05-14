package app.simplecloud.droplet.player.runtime.repository

import app.simplecloud.droplet.player.runtime.database.Database
import app.simplecloud.droplet.player.runtime.entity.OfflinePlayerEntity
import app.simplecloud.droplet.player.runtime.entity.PlayerConnectionEntity
import app.simplecloud.droplet.player.shared.db.tables.OfflinePlayers
import app.simplecloud.droplet.player.shared.db.tables.PlayerConnection
import app.simplecloud.droplet.player.shared.db.tables.records.OfflinePlayersRecord
import app.simplecloud.droplet.player.shared.db.tables.records.PlayerConnectionRecord
import app.simplecloud.droplet.player.shared.db.tables.references.OFFLINE_PLAYERS
import app.simplecloud.droplet.player.shared.db.tables.references.PLAYER_CONNECTION
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.impl.DSL
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class JooqPlayerRepository(
    private val datbase: Database
) : PlayerRepository<OfflinePlayerEntity> {
    override suspend fun save(player: OfflinePlayerEntity) {
        datbase.context.insertInto(
            OfflinePlayers.OFFLINE_PLAYERS,
            OFFLINE_PLAYERS.UNIQUE_ID,
            OFFLINE_PLAYERS.NAME,
            OFFLINE_PLAYERS.DISPLAY_NAME,
            OFFLINE_PLAYERS.FIRST_LOGIN,
            OFFLINE_PLAYERS.LAST_LOGIN,
            OFFLINE_PLAYERS.ONLINE_TIME
        )
            .values(
                player.uniqueId,
                player.name,
                player.displayName,
                LocalDateTime.ofEpochSecond(player.firstLogin / 1000, 0, ZoneOffset.UTC),
                LocalDateTime.ofEpochSecond(player.lastLogin / 1000, 0, ZoneOffset.UTC),
                player.onlineTime
            )
            .onDuplicateKeyUpdate()
            .set(OFFLINE_PLAYERS.NAME, player.name)
            .set(OFFLINE_PLAYERS.DISPLAY_NAME, player.displayName)
            .set(OFFLINE_PLAYERS.FIRST_LOGIN, LocalDateTime.ofEpochSecond(player.firstLogin / 1000, 0, ZoneOffset.UTC))
            .set(OFFLINE_PLAYERS.LAST_LOGIN, LocalDateTime.ofEpochSecond(player.lastLogin / 1000, 0, ZoneOffset.UTC))
            .set(OFFLINE_PLAYERS.ONLINE_TIME, player.onlineTime)
            .executeAsync()
            .thenAccept(
                {
                    saveConnection(player)
                }
            )
            .exceptionally {
                println("Error saving player: $it")
                it.printStackTrace()
                null
            }


    }

    suspend fun findConnectionByUniqueId(uniqueId: UUID): PlayerConnectionEntity? {
        return datbase.context.selectFrom(PLAYER_CONNECTION)
            .where(PLAYER_CONNECTION.UNIQUE_ID.eq(uniqueId.toString()))
            .limit(1)
            .awaitFirstOrNull()
            ?.let {
                mapPlayerConnectionsRecordToEntity(it)
            }

    }

    private fun saveConnection(connection: OfflinePlayerEntity) {
        datbase.context.insertInto(
            PlayerConnection.PLAYER_CONNECTION,
            PLAYER_CONNECTION.UNIQUE_ID,
            PLAYER_CONNECTION.NUMERICAL_CLIENT_VERSION,
            PLAYER_CONNECTION.ONLINE_MODE,
            PLAYER_CONNECTION.CLIENT_LANGUAGE,
            PLAYER_CONNECTION.LAST_SERVER,
            PLAYER_CONNECTION.ONLINE
        )
            .values(
                connection.uniqueId,
                connection.lastPlayerConnection.numericalClientVersion,
                connection.lastPlayerConnection.onlineMode,
                connection.lastPlayerConnection.clientLanguage,
                connection.lastPlayerConnection.lastServer,
                connection.lastPlayerConnection.online
            )
            .onDuplicateKeyUpdate()
            .set(PLAYER_CONNECTION.NUMERICAL_CLIENT_VERSION, connection.lastPlayerConnection.numericalClientVersion)
            .set(PLAYER_CONNECTION.CLIENT_LANGUAGE, connection.lastPlayerConnection.clientLanguage)
            .set(PLAYER_CONNECTION.ONLINE_MODE, connection.lastPlayerConnection.onlineMode)
            .set(PLAYER_CONNECTION.LAST_SERVER, connection.lastPlayerConnection.lastServer)
            .set(PLAYER_CONNECTION.ONLINE, connection.lastPlayerConnection.online)
            .executeAsync().exceptionally {
                println("Error saving connection: $it")
                it.printStackTrace()
                null
            }
    }

    override suspend fun updateDisplayName(uniqueId: UUID, displayName: String) {
        datbase.context.update(OfflinePlayers.OFFLINE_PLAYERS)
            .set(OfflinePlayers.OFFLINE_PLAYERS.DISPLAY_NAME, displayName)
            .where(OfflinePlayers.OFFLINE_PLAYERS.UNIQUE_ID.eq(uniqueId.toString()))
            .executeAsync()
    }

    override suspend fun updateCurrentServer(uniqueId: UUID, serverName: String) {
        datbase.context.update(PLAYER_CONNECTION)
            .set(PLAYER_CONNECTION.LAST_SERVER, serverName)
            .where(PLAYER_CONNECTION.UNIQUE_ID.eq(uniqueId.toString()))
            .executeAsync()
    }

    override suspend fun findByName(name: String): OfflinePlayerEntity? {
        return datbase.context.selectFrom(OfflinePlayers.OFFLINE_PLAYERS)
            .where(DSL.lower(OfflinePlayers.OFFLINE_PLAYERS.NAME).eq(name.lowercase()))
            .awaitFirstOrNull()
            ?.let { mapOfflinePlayersRecordToEntity(it) }
    }

    override suspend fun findByUniqueId(uniqueId: UUID): OfflinePlayerEntity? {
        return findByUniqueId(uniqueId.toString())
    }

    override suspend fun findByUniqueId(uniqueId: String): OfflinePlayerEntity? {
        return datbase.context.selectFrom(OfflinePlayers.OFFLINE_PLAYERS)
            .where(OfflinePlayers.OFFLINE_PLAYERS.UNIQUE_ID.eq(uniqueId))
            .awaitFirstOrNull()
            ?.let { mapOfflinePlayersRecordToEntity(it) }

    }

    override suspend fun findAll(): List<OfflinePlayerEntity> {
        val fetchInto = datbase.context.selectFrom(OfflinePlayers.OFFLINE_PLAYERS)
            .asFlow()
            .toCollection(mutableListOf())
        return fetchInto.map { mapOfflinePlayersRecordToEntity(it) }
    }

    override suspend fun count(): Int {
        return datbase.context.fetchCount(OfflinePlayers.OFFLINE_PLAYERS)
    }

    override suspend fun delete(player: OfflinePlayerEntity) {
        datbase.context.deleteFrom(OfflinePlayers.OFFLINE_PLAYERS)
            .where(OfflinePlayers.OFFLINE_PLAYERS.UNIQUE_ID.eq(player.uniqueId))
            .executeAsync()
    }

    private fun mapOfflinePlayersRecordToEntity(record: OfflinePlayersRecord): OfflinePlayerEntity {
        val lastPlayerConnection = datbase.context.selectFrom(PlayerConnection.PLAYER_CONNECTION)
            .where(PLAYER_CONNECTION.UNIQUE_ID.eq(record.uniqueId))
            .fetchInto(PlayerConnectionRecord::class.java)

        return OfflinePlayerEntity(
            record.uniqueId!!,
            record.name!!,
            record.displayName,
            record.firstLogin!!.toEpochSecond(ZoneOffset.UTC) * 1000,
            record.lastLogin!!.toEpochSecond(ZoneOffset.UTC) * 1000,
            record.onlineTime!!,
            mapPlayerConnectionsRecordToEntity(lastPlayerConnection.first())
        )
    }

    private fun mapPlayerConnectionsRecordToEntity(record: PlayerConnectionRecord): PlayerConnectionEntity {
        return PlayerConnectionEntity(
            record.clientLanguage!!,
            record.numericalClientVersion!!,
            record.onlineMode!!,
            record.lastServer!!,
            record.online!!
        )
    }

}
