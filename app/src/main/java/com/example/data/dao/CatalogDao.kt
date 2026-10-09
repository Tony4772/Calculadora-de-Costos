package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CatalogItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalog_items ORDER BY name ASC")
    fun getAllCatalogItems(): Flow<List<CatalogItem>>

    @Query("SELECT * FROM catalog_items WHERE id = :id")
    fun getCatalogItemById(id: Long): Flow<CatalogItem?>

    @Query("SELECT * FROM catalog_items WHERE category = :category ORDER BY name ASC")
    fun getCatalogItemsByCategory(category: String): Flow<List<CatalogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CatalogItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CatalogItem>)

    @Update
    suspend fun update(item: CatalogItem)

    @Delete
    suspend fun delete(item: CatalogItem)

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM catalog_items")
    suspend fun count(): Int
}
