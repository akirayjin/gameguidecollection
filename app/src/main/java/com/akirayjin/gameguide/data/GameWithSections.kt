package com.akirayjin.gameguide.data

import androidx.room.Embedded
import androidx.room.Relation

data class GameWithSections(
    @Embedded val game: GameEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "gameId"
    )
    val sections: List<GuideSectionEntity>
)
