package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecipeItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeItemDao {
    @Query("SELECT * FROM recipe_items WHERE recipeId = :recipeId ORDER BY sortOrder ASC, id ASC")
    fun getItemsForRecipe(recipeId: Long): Flow<List<RecipeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecipeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<RecipeItem>)

    @Update
    suspend fun update(item: RecipeItem)

    @Delete
    suspend fun delete(item: RecipeItem)

    @Query("DELETE FROM recipe_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recipe_items WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)
}
