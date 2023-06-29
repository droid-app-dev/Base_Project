package com.droid_app_dev.baseproject.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): RoomDataBase {
        return Room.databaseBuilder(context, RoomDataBase::class.java, "sample.db").build()
    }

    @Provides
    fun provideMovieDao(baseDao: RoomDataBase): SampleDao {
        return baseDao.smapleDao()
    }


}