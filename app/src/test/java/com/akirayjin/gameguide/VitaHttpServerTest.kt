package com.akirayjin.gameguide

import com.akirayjin.gameguide.data.GameEntity
import com.akirayjin.gameguide.data.GuideSectionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VitaHttpServerTest {

    @Test
    fun testSectionEntityCreation() {
        val section = GuideSectionEntity(
            gameId = 1L,
            title = "Walkthrough Chapter 1",
            content = "<h2>Chapter 1</h2><p>Defeat the boss and proceed.</p>",
            orderIndex = 1
        )

        assertEquals(1L, section.gameId)
        assertEquals("Walkthrough Chapter 1", section.title)
        assertNotNull(section.content)
        assertEquals(1, section.orderIndex)
    }

    @Test
    fun testGameEntityCreation() {
        val game = GameEntity(
            id = 1L,
            title = "Persona 4 Golden",
            platform = "PS Vita",
            summary = "Best RPG guide"
        )

        assertEquals(1L, game.id)
        assertEquals("Persona 4 Golden", game.title)
        assertEquals("PS Vita", game.platform)
        assertEquals("Best RPG guide", game.summary)
    }
}
