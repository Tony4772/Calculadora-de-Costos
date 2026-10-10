package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogItem
import com.example.data.model.Recipe
import com.example.data.model.RecipeFinancials
import com.example.data.model.RecipeItemDetail
import com.example.ui.components.formatCurrency
import com.example.ui.theme.PrimaryGold
import java.util.Locale

@Composable
fun RecipeBuilderScreen(
    activeRecipe: Recipe?,
    recipes: List<Recipe>,
    recipeItems: List<RecipeItemDetail>,
    catalogItems: List<CatalogItem>,
    financials: RecipeFinancials?,
    currencySymbol: String,
    onSelectRecipe: (Long) -> Unit,
    onUpdateRecipeInfo: (name: String, description: String) -> Unit,
    onAddItem: (catalogItemId: Long, quantity: Double) -> Unit,
    onUpdateQuantity: (recipeItemId: Long, newQuantity: Double) -> Unit,
    onDeleteItem: (recipeItemId: Long) -> Unit,
    onNavigateToResults: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddElementDialogOpen by remember { mutableStateOf(false) }
    var isEditRecipeInfoDialogOpen by remember { mutableStateOf(false) }

    if (activeRecipe == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay receta seleccionada")
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Recipe Header Banner with Edit capability
            RecipeHeaderCard(
                recipe = activeRecipe,
                recipes = recipes,
                totalCost = financials?.directMaterialsCost ?: 0.0,
                itemCount = recipeItems.size,
                currencySymbol = currencySymbol,
                onSelectRecipe = onSelectRecipe,
                onEditRecipe = { isEditRecipeInfoDialogOpen = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: "+ Agregar elemento a la ración"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Elementos de la ración",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = { isAddElementDialogOpen = true },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_element_to_recipe_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Agregar elemento", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items list
            if (recipeItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Esta ración aún no tiene elementos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Agrega insumos o empaques de tu catálogo para calcular el costo de la receta.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { isAddElementDialogOpen = true }) {
                            Text("+ Agregar primer elemento")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header explanation
                    item {
                        Text(
                            text = "En cada ración, el costo unitario del catálogo se multiplica por la cantidad consumida.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(recipeItems, key = { it.recipeItem.id }) { itemDetail ->
                        RecipeItemRowCard(
                            itemDetail = itemDetail,
                            currencySymbol = currencySymbol,
                            onUpdateQuantity = { newQty ->
                                onUpdateQuantity(itemDetail.recipeItem.id, newQty)
                            },
                            onDelete = {
                                onDeleteItem(itemDetail.recipeItem.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }

        // Floating Bottom Summary Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Costo primo directo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(financials?.directMaterialsCost ?: 0.0, currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onNavigateToResults,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("goto_results_btn")
                ) {
                    Text(text = "Ver Resultado & Margen", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Dialog to add item from catalog
    if (isAddElementDialogOpen) {
        AddElementToRecipeDialog(
            catalogItems = catalogItems,
            existingItemIds = recipeItems.map { it.catalogItem.id }.toSet(),
            currencySymbol = currencySymbol,
            onDismiss = { isAddElementDialogOpen = false },
            onAdd = { catId, qty ->
                onAddItem(catId, qty)
                isAddElementDialogOpen = false
            },
            onNavigateToCatalog = {
                isAddElementDialogOpen = false
                onNavigateToCatalog()
            }
        )
    }

    // Dialog to edit recipe name/description
    if (isEditRecipeInfoDialogOpen) {
        EditRecipeInfoDialog(
            currentName = activeRecipe.name,
            currentDescription = activeRecipe.description,
            onDismiss = { isEditRecipeInfoDialogOpen = false },
            onSave = { name, desc ->
                onUpdateRecipeInfo(name, desc)
                isEditRecipeInfoDialogOpen = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeHeaderCard(
    recipe: Recipe,
    recipes: List<Recipe>,
    totalCost: Double,
    itemCount: Int,
    currencySymbol: String,
    onSelectRecipe: (Long) -> Unit,
    onEditRecipe: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dropdown to pick recipe
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .menuAnchor()
                            .clickable { expandedDropdown = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                    }

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        recipes.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.name, fontWeight = if (r.id == recipe.id) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    onSelectRecipe(r.id)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onEditRecipe,
                    modifier = Modifier.testTag("edit_recipe_info_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar nombre",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (recipe.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$itemCount elementos incluidos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Subtotal: ${formatCurrency(totalCost, currencySymbol)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RecipeItemRowCard(
    itemDetail: RecipeItemDetail,
    currencySymbol: String,
    onUpdateQuantity: (Double) -> Unit,
    onDelete: () -> Unit
) {
    val catalog = itemDetail.catalogItem
    val recipeItem = itemDetail.recipeItem

    var isEditingQuantity by remember { mutableStateOf(false) }
    var quantityInputText by remember {
        mutableStateOf(String.format(Locale.US, if (recipeItem.quantity % 1 == 0.0) "%.0f" else "%.2f", recipeItem.quantity))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recipe_item_card_${recipeItem.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Name and Category badge and Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = catalog.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${catalog.category} • Costo base: ${catalog.formatUnitCost(currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_recipe_item_${recipeItem.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Quitar elemento",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Quantity Editor with Stepper buttons & Subtotal display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quantity editor with Stepper
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Decrease button
                    IconButton(
                        onClick = {
                            val step = if (catalog.unit.contains("Gramo", ignoreCase = true) || catalog.unit.contains("Mili", ignoreCase = true)) 10.0 else 1.0
                            val newQty = (recipeItem.quantity - step).coerceAtLeast(0.1)
                            quantityInputText = String.format(Locale.US, if (newQty % 1 == 0.0) "%.0f" else "%.2f", newQty)
                            onUpdateQuantity(newQty)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Disminuir",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Direct input text box
                    OutlinedTextField(
                        value = quantityInputText,
                        onValueChange = { input ->
                            quantityInputText = input
                            input.toDoubleOrNull()?.let { valid ->
                                if (valid > 0) onUpdateQuantity(valid)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .width(72.dp)
                            .height(44.dp)
                            .testTag("qty_input_${recipeItem.id}"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Increase button
                    IconButton(
                        onClick = {
                            val step = if (catalog.unit.contains("Gramo", ignoreCase = true) || catalog.unit.contains("Mili", ignoreCase = true)) 10.0 else 1.0
                            val newQty = recipeItem.quantity + step
                            quantityInputText = String.format(Locale.US, if (newQty % 1 == 0.0) "%.0f" else "%.2f", newQty)
                            onUpdateQuantity(newQty)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aumentar",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = catalog.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Cost Formula Display: 60 × S/ 0.020 = S/ 1.20
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = formatCurrency(itemDetail.subtotalCost, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f × %.3f", recipeItem.quantity, catalog.unitCost),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddElementToRecipeDialog(
    catalogItems: List<CatalogItem>,
    existingItemIds: Set<Long>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAdd: (catalogItemId: Long, quantity: Double) -> Unit,
    onNavigateToCatalog: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<CatalogItem?>(null) }
    var quantityText by remember { mutableStateOf("1") }

    val filteredItems = remember(catalogItems, searchQuery) {
        catalogItems.filter {
            searchQuery.isBlank() ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val parsedQty = quantityText.toDoubleOrNull() ?: 1.0
    val calculatedSubtotal = (selectedItem?.unitCost ?: 0.0) * parsedQty

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar elemento a la ración",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (catalogItems.isEmpty()) {
                    Text("Tu catálogo no tiene insumos registrados.")
                    Button(onClick = onNavigateToCatalog) {
                        Text("Ir al catálogo a registrar")
                    }
                    return@Column
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar en catálogo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // List of catalog items to select from
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        val isSelected = selectedItem?.id == item.id
                        val isAlreadyAdded = existingItemIds.contains(item.id)

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItem = item },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${item.category} • ${item.formatUnitCost(currencySymbol)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isAlreadyAdded) {
                                    Text(
                                        text = "Ya en ración",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // If an item is selected, show quantity input & preview
                if (selectedItem != null) {
                    val item = selectedItem!!
                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Cantidad (${item.unit})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("modal_item_qty_input")
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Subtotal:",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = formatCurrency(calculatedSubtotal, currencySymbol),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val item = selectedItem
                    if (item != null && parsedQty > 0) {
                        onAdd(item.id, parsedQty)
                    }
                },
                enabled = selectedItem != null && parsedQty > 0,
                modifier = Modifier.testTag("confirm_add_element_btn")
            ) {
                Text("Añadir a Ración")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditRecipeInfoDialog(
    currentName: String,
    currentDescription: String,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var description by remember { mutableStateOf(currentDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Ración / Receta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la ración o producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción / Porción (opcional)") },
                    placeholder = { Text("Ej: 1 porción / 1 unidad terminada / 1 paquete") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onSave(name.trim(), description.trim())
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
