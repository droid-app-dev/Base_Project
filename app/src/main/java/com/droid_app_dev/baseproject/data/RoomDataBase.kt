package com.droid_app_dev.baseproject.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SampleEntinty::class,], version = 1, exportSchema = false)
abstract class RoomDataBase:RoomDatabase() {

    abstract fun smapleDao(): SampleDao




}