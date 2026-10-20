package com.akirayjin.gameguide.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Transaction
    @Query("SELECT * FROM games ORDER BY updatedAt DESC")
    fun getAllGamesWithSections(): Flow<List<GameWithSections>>

    @Transaction
    @Query("SELECT * FROM games ORDER BY updatedAt DESC")
    suspend fun getAllGamesWithSectionsSync(): List<GameWithSections>

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId LIMIT 1")
    suspend fun getGameWithSectionsById(gameId: Long): GameWithSections?

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: Long): GameEntity?

    @Query("SELECT * FROM guide_sections WHERE id = :id LIMIT 1")
    suspend fun getSectionById(id: Long): GuideSectionEntity?

    @Query("SELECT count(*) FROM games")
    suspend fun getGameCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity): Int

    @Delete
    suspend fun deleteGame(game: GameEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: GuideSectionEntity): Long

    @Update
    suspend fun updateSection(section: GuideSectionEntity): Int

    @Delete
    suspend fun deleteSection(section: GuideSectionEntity): Int

    @Query("""
        SELECT DISTINCT g.* FROM games g 
        LEFT JOIN guide_sections s ON g.id = s.gameId 
        WHERE g.title LIKE '%' || :query || '%' 
           OR g.summary LIKE '%' || :query || '%' 
           OR s.title LIKE '%' || :query || '%' 
           OR s.content LIKE '%' || :query || '%'
    """)
    suspend fun searchGames(query: String): List<GameEntity>
}
