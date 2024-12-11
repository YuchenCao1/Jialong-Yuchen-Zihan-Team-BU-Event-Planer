package com.example.bueventplaner.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.bueventplaner.utils.Converters

@Entity(tableName = "event_table")
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val location: String,
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "end_time") val endTime: String,
    val photo: String,
    val eventUrl: String,
    @ColumnInfo(name = "savedUsers") val savedUsers: List<String> // TypeConverter 必须正确配置
)

