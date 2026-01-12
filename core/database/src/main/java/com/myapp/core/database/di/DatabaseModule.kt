package com.myapp.core.database.di

import android.content.Context
import androidx.room.Room
import com.myapp.core.database.MyAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesMyAppDatabase(
        @ApplicationContext context: Context,
    ): MyAppDatabase = Room.databaseBuilder(
        context,
        MyAppDatabase::class.java,
        "myapp-database",
    ).build()
}
