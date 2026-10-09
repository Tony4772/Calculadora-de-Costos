package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CatalogItem
import com.example.data.model.CostCalculatorEngine
import com.example.data.model.Recipe
import com.example.data.model.RecipeFinancials
import com.example.data.model.RecipeItem
import com.example.data.model.RecipeItemDetail
import com.example.data.repository.CostCalculatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    CATALOG("Catálogo"),
    RECIPE("Armar Ración"),
    RESULTS("Resultados"),
    RECIPES_LIST("Mis Recetas")
}

class CostViewModel(
    private val repository: CostCalculatorRepository
) : ViewModel() {

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.RECIPE)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Currency symbol
    private val _currencySymbol = MutableStateFlow("S/")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    // Catalog items
    val catalogItems: StateFlow<List<CatalogItem>> = repository.allCatalogItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All recipes
    val allRecipes: StateFlow<List<Recipe>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active recipe ID
    private val _selectedRecipeId = MutableStateFlow<Long?>(null)
    val selectedRecipeId: StateFlow<Long?> = _selectedRecipeId.asStateFlow()

    // Active recipe
    private val _activeRecipe = MutableStateFlow<Recipe?>(null)
    val activeRecipe: StateFlow<Recipe?> = _activeRecipe.asStateFlow()

    // Items for active recipe
    private val _rawRecipeItems = MutableStateFlow<List<RecipeItem>>(emptyList())

    // Recipe item details combined with catalog data
    val recipeItemDetails: StateFlow<List<RecipeItemDetail>> = combine(
        _rawRecipeItems,
        catalogItems
    ) { items, catalog ->
        val catalogMap = catalog.associateBy { it.id }
        items.mapNotNull { item ->
            val catItem = catalogMap[item.catalogItemId]
            if (catItem != null) {
                RecipeItemDetail(recipeItem = item, catalogItem = catItem)
            } else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live financial calculation
    val recipeFinancials: StateFlow<RecipeFinancials?> = combine(
        _activeRecipe,
        recipeItemDetails
    ) { recipe, details ->
        if (recipe != null) {
            CostCalculatorEngine.calculate(recipe, details)
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Catalog search & filter state
    val catalogSearchQuery = MutableStateFlow("")
    val catalogCategoryFilter = MutableStateFlow("TODOS")

    init {
        viewModelScope.launch {
            // Seed sample data if database is brand new
            repository.seedInitialDataIfEmpty()

            // Observe first recipe to set as default active recipe
            val first = repository.getFirstRecipe()
            if (first != null && _selectedRecipeId.value == null) {
                selectRecipe(first.id)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun selectRecipe(recipeId: Long) {
        _selectedRecipeId.value = recipeId
        viewModelScope.launch {
            repository.getRecipeById(recipeId).collect { recipe ->
                _activeRecipe.value = recipe
            }
        }
        viewModelScope.launch {
            repository.getItemsForRecipe(recipeId).collect { items ->
                _rawRecipeItems.value = items
            }
        }
    }

    // Catalog CRUD
    fun saveCatalogItem(
        id: Long = 0,
        name: String,
        category: String,
        purchasePrice: Double,
        purchaseQuantity: Double,
        unit: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val item = CatalogItem(
                id = id,
                name = name.trim(),
                category = category,
                purchasePrice = purchasePrice,
                purchaseQuantity = purchaseQuantity,
                unit = unit,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertCatalogItem(item)
            } else {
                repository.updateCatalogItem(item)
            }
        }
    }

    fun deleteCatalogItem(item: CatalogItem) {
        viewModelScope.launch {
            repository.deleteCatalogItem(item)
        }
    }

    // Recipe Item CRUD
    fun addItemToRecipe(catalogItemId: Long, quantity: Double) {
        val currentRecId = _selectedRecipeId.value ?: return
        viewModelScope.launch {
            // Check if already in recipe, if so, update quantity
            val existing = _rawRecipeItems.value.find { it.catalogItemId == catalogItemId }
            if (existing != null) {
                val updated = existing.copy(quantity = existing.quantity + quantity)
                repository.updateRecipeItem(updated)
            } else {
                val newItem = RecipeItem(
                    recipeId = currentRecId,
                    catalogItemId = catalogItemId,
                    quantity = quantity,
                    sortOrder = _rawRecipeItems.value.size + 1
                )
                repository.insertRecipeItem(newItem)
            }
        }
    }

    fun updateRecipeItemQuantity(recipeItemId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            deleteRecipeItem(recipeItemId)
            return
        }
        viewModelScope.launch {
            val item = _rawRecipeItems.value.find { it.id == recipeItemId } ?: return@launch
            repository.updateRecipeItem(item.copy(quantity = newQuantity))
        }
    }

    fun deleteRecipeItem(recipeItemId: Long) {
        viewModelScope.launch {
            repository.deleteRecipeItemById(recipeItemId)
        }
    }

    // Recipe modifications
    fun updateActiveRecipe(
        name: String? = null,
        description: String? = null,
        salePrice: Double? = null,
        wastePercentage: Double? = null,
        taxPercentage: Double? = null,
        isTaxIncludedInPrice: Boolean? = null,
        gatewayName: String? = null,
        gatewayFixedCommission: Double? = null,
        gatewayCommissionPercentage: Double? = null,
        laborCost: Double? = null
    ) {
        val current = _activeRecipe.value ?: return
        val updated = current.copy(
            name = name ?: current.name,
            description = description ?: current.description,
            salePrice = salePrice ?: current.salePrice,
            wastePercentage = wastePercentage ?: current.wastePercentage,
            taxPercentage = taxPercentage ?: current.taxPercentage,
            isTaxIncludedInPrice = isTaxIncludedInPrice ?: current.isTaxIncludedInPrice,
            gatewayName = gatewayName ?: current.gatewayName,
            gatewayFixedCommission = gatewayFixedCommission ?: current.gatewayFixedCommission,
            gatewayCommissionPercentage = gatewayCommissionPercentage ?: current.gatewayCommissionPercentage,
            laborCost = laborCost ?: current.laborCost,
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.updateRecipe(updated)
            _activeRecipe.value = updated
        }
    }

    fun createNewRecipe(name: String, description: String = "") {
        viewModelScope.launch {
            val newRecipe = Recipe(
                name = name.ifBlank { "Nueva Receta" },
                description = description,
                portions = 1,
                salePrice = 0.0,
                wastePercentage = 0.0,
                taxPercentage = 18.0,
                isTaxIncludedInPrice = true,
                gatewayFixedCommission = 0.0
            )
            val newId = repository.insertRecipe(newRecipe)
            selectRecipe(newId)
            _currentTab.value = AppTab.RECIPE
        }
    }

    fun duplicateRecipe(recipeId: Long, newName: String) {
        viewModelScope.launch {
            val newId = repository.duplicateRecipe(recipeId, newName)
            if (newId > 0) {
                selectRecipe(newId)
                _currentTab.value = AppTab.RECIPE
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
            val remaining = repository.allRecipes.first().filter { it.id != recipe.id }
            if (remaining.isNotEmpty()) {
                selectRecipe(remaining.first().id)
            } else {
                createNewRecipe("Mi Primer Producto")
            }
        }
    }

    fun applyTargetMargin(targetMarginPct: Double) {
        val rec = _activeRecipe.value ?: return
        val fin = recipeFinancials.value ?: return
        val suggestedPrice = CostCalculatorEngine.suggestPriceForMargin(rec, fin.totalCost, targetMarginPct)
        // Round to 2 decimal places
        val roundedPrice = (Math.round(suggestedPrice * 100.0) / 100.0)
        updateActiveRecipe(salePrice = roundedPrice)
    }

    class Factory(private val repository: CostCalculatorRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CostViewModel::class.java)) {
                return CostViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
