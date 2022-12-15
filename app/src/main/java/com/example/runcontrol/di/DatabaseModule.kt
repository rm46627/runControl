package com.example.runcontrol.di

import android.content.Context
import androidx.room.Room
import com.example.runcontrol.database.Database
import com.example.runcontrol.utils.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        Database::class.java,
        DATABASE_NAME)
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideRunDao(database: Database) = database.runDao()

}