package BugBug.androidApp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId")]
)
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: Long,
    val score: Int,
    val hits: Int,
    val misses: Int,
    val difficulty: Int,
    val roundDurationSec: Int,
    val playedAt: Long = System.currentTimeMillis()
) {
    val accuracyPercent: Float
        get() {
            val total = hits + misses
            return if (total == 0) 0f else hits.toFloat() / total * 100f
        }
}