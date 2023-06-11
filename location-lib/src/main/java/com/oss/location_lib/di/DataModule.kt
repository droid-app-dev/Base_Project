package com.oss.location_lib.di

import android.content.Context
import android.content.SharedPreferences
import com.oss.location_lib.data.FusedLocation
import com.oss.location_lib.data.SharedLocationManager
import com.oss.location_lib.data.SharedGnssStatusManager
import com.oss.location_lib.data.SharedNmeaManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.GlobalScope
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSharedLocationManager(
        @ApplicationContext context: Context,
    ): SharedLocationManager =
        SharedLocationManager(context, GlobalScope)

    @Provides
    @Singleton
    fun provideFusedLocationclient(
        @ApplicationContext context: Context
    ): FusedLocation = FusedLocation(context, GlobalScope)

    @Provides
    @Singleton
    fun provideSharedGnssStatusManager(
        @ApplicationContext context: Context,
    ): SharedGnssStatusManager =
        SharedGnssStatusManager(context, GlobalScope)

    @Provides
    @Singleton
    fun provideSharedNmeaManager(
        @ApplicationContext context: Context): SharedNmeaManager =
        SharedNmeaManager(context, GlobalScope)



}