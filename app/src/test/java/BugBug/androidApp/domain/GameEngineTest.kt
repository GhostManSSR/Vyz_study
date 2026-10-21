package BugBug.androidApp.domain

import BugBug.androidApp.model.Insect
import BugBug.androidApp.model.InsectType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val centipede = Insect(
        id = 1L,
        type = InsectType.CENTIPEDE,
        position = Offset(20f, 30f),
        velocity = Offset(3f, 1f),
        size = Size(560f, 180f)
    )

    @Test
    fun centipedeCanBeHitAtBothEndsOfItsBody() {
        assertTrue(GameEngine.isHit(centipede, Offset(76f, 120f)))
        assertTrue(GameEngine.isHit(centipede, Offset(524f, 120f)))
        assertFalse(GameEngine.isHit(centipede, Offset(20f, 120f)))
    }

    @Test
    fun splittingCentipedeLeavesTwoCaterpillarsInsideTheField() {
        val children = GameEngine.splitInsect(
            insect = centipede,
            fieldSize = Size(800f, 400f),
            firstId = 10L
        )

        assertEquals(2, children.size)
        assertEquals(listOf(10L, 11L), children.map { it.id })
        assertTrue(children.all { it.type == InsectType.CATERPILLAR })
        assertTrue(children.all {
            it.position.x >= 0f &&
                it.position.y >= 0f &&
                it.position.x + it.size.width <= 800f &&
                it.position.y + it.size.height <= 400f
        })
    }

    @Test
    fun armoredBeetleAndSpiderSplitIntoScoringInsects() {
        val fieldSize = Size(800f, 600f)
        val parents = listOf(
            InsectType.ARMORED_BEETLE to InsectType.BUG,
            InsectType.SPIDER to InsectType.FLY
        )

        parents.forEachIndexed { index, (parentType, childType) ->
            val parent = centipede.copy(
                id = index.toLong(),
                type = parentType,
                position = Offset(200f, 200f),
                size = Size(120f, 120f)
            )
            val children = GameEngine.splitInsect(parent, fieldSize, 20L + index * 2)

            assertEquals(2, children.size)
            assertTrue(children.all { it.type == childType })
            assertTrue(children.all { it.type.points > 0 })
            assertEquals(0, parentType.points)
        }
    }
}
