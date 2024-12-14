package com.example.bueventplaner.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.bueventplaner.data.model.EventEntity

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvents(events: List<EventEntity>)

    @Query("SELECT * FROM event_table")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event_table WHERE id = :eventId")
    fun getEventById(eventId: String): Flow<EventEntity?>

    @Query("DELETE FROM event_table")
    fun deleteAllEvents()

}

