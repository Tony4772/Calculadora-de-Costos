package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CatalogDao
import com.example.data.dao.RecipeDao
import com.example.data.dao.RecipeItemDao
import com.example.data.model.CatalogItem
import com.example.data.model.Recipe
import com.example.data.model.RecipeItem

@Database(
    entities = [
        CatalogItem::class,
        Recipe::class,
        RecipeItem::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeItemDao(): RecipeItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cost_calculator_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
