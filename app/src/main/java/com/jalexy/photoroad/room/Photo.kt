package com.jalexy.photoroad.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey @ColumnInfo(name="uri") val photoUri: String,
    @ColumnInfo(name="sent") val sent: Boolean)