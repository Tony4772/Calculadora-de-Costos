package com.example.data.repository

import com.example.data.dao.CatalogDao
import com.example.data.dao.RecipeDao
import com.example.data.dao.RecipeItemDao
import com.example.data.model.CatalogItem
import com.example.data.model.Recipe
import com.example.data.model.RecipeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CostCalculatorRepository(
    private val catalogDao: CatalogDao,
    private val recipeDao: RecipeDao,
    private val recipeItemDao: RecipeItemDao
) {
    val allCatalogItems: Flow<List<CatalogItem>> = catalogDao.getAllCatalogItems()
    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    fun getRecipeById(recipeId: Long): Flow<Recipe?> = recipeDao.getRecipeById(recipeId)

    fun getItemsForRecipe(recipeId: Long): Flow<List<RecipeItem>> =
        recipeItemDao.getItemsForRecipe(recipeId)

    // Catalog operations
    suspend fun insertCatalogItem(item: CatalogItem): Long = catalogDao.insert(item)

    suspend fun updateCatalogItem(item: CatalogItem) = catalogDao.update(item)

    suspend fun deleteCatalogItem(item: CatalogItem) = catalogDao.delete(item)

    suspend fun deleteCatalogItemById(id: Long) = catalogDao.deleteById(id)

    // Recipe operations
    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insert(recipe)

    suspend fun updateRecipe(recipe: Recipe) = recipeDao.update(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.delete(recipe)

    suspend fun getFirstRecipe(): Recipe? = recipeDao.getFirstRecipe()

    // Recipe item operations
    suspend fun insertRecipeItem(item: RecipeItem): Long = recipeItemDao.insert(item)

    suspend fun updateRecipeItem(item: RecipeItem) = recipeItemDao.update(item)

    suspend fun deleteRecipeItem(item: RecipeItem) = recipeItemDao.delete(item)

    suspend fun deleteRecipeItemById(id: Long) = recipeItemDao.deleteById(id)

    suspend fun duplicateRecipe(recipeId: Long, newName: String): Long {
        val originalRecipe = recipeDao.getRecipeById(recipeId).first() ?: return -1
        val duplicatedRecipe = originalRecipe.copy(
            id = 0,
            name = newName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newRecipeId = recipeDao.insert(duplicatedRecipe)
        val originalItems = recipeItemDao.getItemsForRecipe(recipeId).first()
        val duplicatedItems = originalItems.map { it.copy(id = 0, recipeId = newRecipeId) }
        recipeItemDao.insertAll(duplicatedItems)
        return newRecipeId
    }

    suspend fun seedInitialDataIfEmpty() {
        // Starts with a clean, neutral template. No business-specific or hardcoded product items.
        if (recipeDao.count() == 0) {
            val initialRecipe = Recipe(
                name = "Mi Primer Producto",
                description = "1 porción o unidad terminada",
                portions = 1,
                salePrice = 0.0,
                wastePercentage = 0.0,
                taxPercentage = 18.0,
                isTaxIncludedInPrice = true,
                gatewayName = "Sin comisión (Efectivo)",
                gatewayCommissionPercentage = 0.0,
                gatewayFixedCommission = 0.0,
                laborCost = 0.0
            )
            recipeDao.insert(initialRecipe)
        }
    }
}
