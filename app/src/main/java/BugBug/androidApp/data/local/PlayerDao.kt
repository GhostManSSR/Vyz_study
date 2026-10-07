package BugBug.androidApp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players WHERE fullName = :name LIMIT 1")
    suspend fun getPlayerByName(name: String): PlayerEntity?

    @Insert
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Query("SELECT * FROM players ORDER BY createdAt DESC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: Long): PlayerEntity?

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)

    @Insert
    suspend fun insertScore(score: ScoreEntity): Long

    @Query("""
    SELECT p.fullName AS fullName,
           s.score AS score,
           s.difficulty AS difficulty,
           s.playedAt AS playedAt
    FROM scores s
    INNER JOIN players p ON p.id = s.playerId
    WHERE s.id = (
        SELECT s2.id FROM scores s2
        WHERE s2.playerId = s.playerId
        ORDER BY s2.score DESC, s2.playedAt ASC
        LIMIT 1
    )
    ORDER BY s.score DESC
    LIMIT :limit
""")
    fun getTopScores(limit: Int = 5): Flow<List<ScoreRecord>>

    @Query("""
        SELECT * FROM scores
        WHERE playerId = :playerId
        ORDER BY playedAt DESC
    """)
    fun getScoresForPlayer(playerId: Long): Flow<List<ScoreEntity>>
}

data class ScoreRecord(
    val fullName: String,
    val score: Int,
    val difficulty: Int,
    val playedAt: Long
)