package BugBug.androidApp.data.repository

import BugBug.androidApp.data.local.PlayerDao
import BugBug.androidApp.data.local.PlayerEntity
import BugBug.androidApp.data.local.ScoreEntity
import BugBug.androidApp.data.local.ScoreRecord
import BugBug.androidApp.model.Player
import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val dao: PlayerDao) {

    fun getAllPlayers(): Flow<List<PlayerEntity>> = dao.getAllPlayers()

    fun getTopScores(limit: Int = 20): Flow<List<ScoreRecord>> =
        dao.getTopScores(limit)

    fun getScoresForPlayer(playerId: Long): Flow<List<ScoreEntity>> =
        dao.getScoresForPlayer(playerId)

    suspend fun getPlayerById(id: Long): PlayerEntity? = dao.getPlayerById(id)

    suspend fun savePlayer(player: Player): Long =
        dao.insertPlayer(
            PlayerEntity(
                fullName = player.fullName,
                gender = player.gender,
                course = player.course,
                difficulty = player.difficulty,
                birthDate = player.birthDate,
                zodiacName = player.zodiac.title
            )
        )

    suspend fun saveScore(
        playerId: Long,
        score: Int,
        hits: Int,
        misses: Int,
        difficulty: Int,
        roundDurationSec: Int
    ): Long =
        dao.insertScore(
            ScoreEntity(
                playerId = playerId,
                score = score,
                hits = hits,
                misses = misses,
                difficulty = difficulty,
                roundDurationSec = roundDurationSec
            )
        )

    suspend fun deletePlayer(player: PlayerEntity) = dao.deletePlayer(player)
}