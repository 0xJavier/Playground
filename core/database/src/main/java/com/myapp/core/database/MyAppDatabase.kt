package com.myapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the app.
 * Add your entities and DAOs here as you develop features.
 */
@Database(
    entities = [],
    version = 1,
    exportSchema = true,
)
abstract class MyAppDatabase : RoomDatabase() {
    // Add your DAOs here as abstract functions
    // Example: abstract fun userDao(): UserDao
}
