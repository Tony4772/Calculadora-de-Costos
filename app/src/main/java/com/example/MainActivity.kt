package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.CostCalculatorRepository
import com.example.ui.components.BannerAd
import com.example.ui.components.CurrencyPickerModal
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.RecipeBuilderScreen
import com.example.ui.screens.RecipeListScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.CostViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MobileAds.initialize(this) {}

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember {
                    CostCalculatorRepository(
                        catalogDao = database.catalogDao(),
                        recipeDao = database.recipeDao(),
                        recipeItemDao = database.recipeItemDao()
                    )
                }

                val viewModel: CostViewModel = viewModel(
                    factory = CostViewModel.Factory(repository)
                )

                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
                val catalogItems by viewModel.catalogItems.collectAsStateWithLifecycle()
                val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
                val activeRecipe by viewModel.activeRecipe.collectAsStateWithLifecycle()
                val recipeItemDetails by viewModel.recipeItemDetails.collectAsStateWithLifecycle()
                val financials by viewModel.recipeFinancials.collectAsStateWithLifecycle()

                var showCurrencyPicker by remember { mutableStateOf(false) }
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onTimeout = { showSplash = false }
                    )
                } else {
                    // Back navigation handling
                    BackHandler(enabled = currentTab != AppTab.RECIPE) {
                        viewModel.selectTab(AppTab.RECIPE)
                    }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "Calculadora de Costos",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (activeRecipe != null) {
                                        Text(
                                            text = "Ración activa: ${activeRecipe!!.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Currency selector badge
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { showCurrencyPicker = true }
                                        .testTag("currency_selector_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Moneda:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currencySymbol,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BannerAd(
                                adUnitId = "ca-app-pub-7470413991742442/7991867129",
                                modifier = Modifier.fillMaxWidth()
                            )
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == AppTab.CATALOG,
                                    onClick = { viewModel.selectTab(AppTab.CATALOG) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = "Catálogo"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Catálogo",
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            fontSize = 11.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_catalog")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AppTab.RECIPE,
                                    onClick = { viewModel.selectTab(AppTab.RECIPE) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = "Armar Ración"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Armar Ración",
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            fontSize = 11.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_recipe")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AppTab.RESULTS,
                                    onClick = { viewModel.selectTab(AppTab.RESULTS) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Resultados"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Resultados",
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            fontSize = 11.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_results")
                                )

                                NavigationBarItem(
                                    selected = currentTab == AppTab.RECIPES_LIST,
                                    onClick = { viewModel.selectTab(AppTab.RECIPES_LIST) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = "Mis Recetas"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Mis Recetas",
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            fontSize = 11.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        indicatorColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_recipes_list")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.CATALOG -> {
                                CatalogScreen(
                                    catalogItems = catalogItems,
                                    currencySymbol = currencySymbol,
                                    onSaveItem = { id, name, category, price, qty, unit, notes ->
                                        viewModel.saveCatalogItem(id, name, category, price, qty, unit, notes)
                                    },
                                    onDeleteItem = { item ->
                                        viewModel.deleteCatalogItem(item)
                                    },
                                    onAddItemToCurrentRecipe = { catId, qty ->
                                        viewModel.addItemToRecipe(catId, qty)
                                        viewModel.selectTab(AppTab.RECIPE)
                                    }
                                )
                            }

                            AppTab.RECIPE -> {
                                RecipeBuilderScreen(
                                    activeRecipe = activeRecipe,
                                    recipes = allRecipes,
                                    recipeItems = recipeItemDetails,
                                    catalogItems = catalogItems,
                                    financials = financials,
                                    currencySymbol = currencySymbol,
                                    onSelectRecipe = { id -> viewModel.selectRecipe(id) },
                                    onUpdateRecipeInfo = { name, desc ->
                                        viewModel.updateActiveRecipe(name = name, description = desc)
                                    },
                                    onAddItem = { catId, qty ->
                                        viewModel.addItemToRecipe(catId, qty)
                                    },
                                    onUpdateQuantity = { recipeItemId, qty ->
                                        viewModel.updateRecipeItemQuantity(recipeItemId, qty)
                                    },
                                    onDeleteItem = { recipeItemId ->
                                        viewModel.deleteRecipeItem(recipeItemId)
                                    },
                                    onNavigateToResults = {
                                        viewModel.selectTab(AppTab.RESULTS)
                                    },
                                    onNavigateToCatalog = {
                                        viewModel.selectTab(AppTab.CATALOG)
                                    }
                                )
                            }

                            AppTab.RESULTS -> {
                                ResultsScreen(
                                    recipe = activeRecipe,
                                    financials = financials,
                                    items = recipeItemDetails,
                                    currencySymbol = currencySymbol,
                                    onUpdateRecipePricing = { salePrice, wastePct, taxPct, taxInc, gName, gFixed, gPct, labor ->
                                        viewModel.updateActiveRecipe(
                                            salePrice = salePrice,
                                            wastePercentage = wastePct,
                                            taxPercentage = taxPct,
                                            isTaxIncludedInPrice = taxInc,
                                            gatewayName = gName,
                                            gatewayFixedCommission = gFixed,
                                            gatewayCommissionPercentage = gPct,
                                            laborCost = labor
                                        )
                                    },
                                    onApplyTargetMargin = { targetMarginPct ->
                                        viewModel.applyTargetMargin(targetMarginPct)
                                    }
                                )
                            }

                            AppTab.RECIPES_LIST -> {
                                RecipeListScreen(
                                    recipes = allRecipes,
                                    activeRecipeId = activeRecipe?.id,
                                    currencySymbol = currencySymbol,
                                    onSelectRecipe = { id ->
                                        viewModel.selectRecipe(id)
                                        viewModel.selectTab(AppTab.RECIPE)
                                    },
                                    onCreateRecipe = { name, desc ->
                                        viewModel.createNewRecipe(name, desc)
                                    },
                                    onDuplicateRecipe = { id, name ->
                                        viewModel.duplicateRecipe(id, name)
                                    },
                                    onDeleteRecipe = { recipe ->
                                        viewModel.deleteRecipe(recipe)
                                    }
                                )
                            }
                        }
                    }
                }
                }

                if (showCurrencyPicker) {
                    CurrencyPickerModal(
                        currentCurrency = currencySymbol,
                        onSelect = { symbol ->
                            viewModel.setCurrencySymbol(symbol)
                            showCurrencyPicker = false
                        },
                        onDismiss = { showCurrencyPicker = false }
                    )
                }
            }
        }
    }
}
