package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Recipe
import com.example.data.model.RecipeFinancials
import com.example.data.model.RecipeItemDetail
import com.example.ui.components.ProfitabilityBadge
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatPercent
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.ProfitGreen
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    recipe: Recipe?,
    financials: RecipeFinancials?,
    items: List<RecipeItemDetail>,
    currencySymbol: String,
    onUpdateRecipePricing: (
        salePrice: Double?,
        wastePercentage: Double?,
        taxPercentage: Double?,
        isTaxIncluded: Boolean?,
        gatewayName: String?,
        gatewayFixed: Double?,
        gatewayPct: Double?,
        laborCost: Double?
    ) -> Unit,
    onApplyTargetMargin: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (recipe == null || financials == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay datos de receta disponibles para calcular")
        }
        return
    }

    var isEditingPricingSettings by remember { mutableStateOf(false) }
    var isBreakdownExpanded by remember { mutableStateOf(true) }

    // Waste Dropdown state
    var wasteMenuExpanded by remember { mutableStateOf(false) }
    val wasteOptions = listOf(
        0.0 to "Sin merma (0%)",
        3.0 to "3% Merma",
        5.0 to "5% Merma estándar",
        8.0 to "8% Merma",
        10.0 to "10% Merma alta",
        15.0 to "15% Merma muy alta"
    )

    // Sale Price inline text state
    var salePriceText by remember(recipe.salePrice) {
        mutableStateOf(String.format(Locale.US, "%.2f", recipe.salePrice))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Recipe Name
        item {
            Column {
                Text(
                    text = "Resultados y margen",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${recipe.name} • ${if (recipe.description.isNotBlank()) recipe.description else "Costeo de producción"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Itemized Cost Breakdown Card (Collapsible)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBreakdownExpanded = !isBreakdownExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Desglose de elementos (${items.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Icon(
                            imageVector = if (isBreakdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = isBreakdownExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (items.isEmpty()) {
                                Text(
                                    text = "No has agregado elementos a la ración todavía.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                items.forEach { item ->
                                    val cat = item.catalogItem
                                    val qtyFormatted = if (item.recipeItem.quantity % 1 == 0.0) {
                                        String.format(Locale.US, "%.0f", item.recipeItem.quantity)
                                    } else {
                                        String.format(Locale.US, "%.2f", item.recipeItem.quantity)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${cat.name.lowercase()}: $qtyFormatted × ${formatCurrency(cat.unitCost, currencySymbol)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formatCurrency(item.subtotalCost, currencySymbol),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Row 1 of financial cards: Costo Primo Directo & Merma Opcional
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Costo Primo Directo
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Costo primo directo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(financials.directMaterialsCost, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Merma Opcional Selector
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Merma opcional",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = wasteMenuExpanded,
                            onExpandedChange = { wasteMenuExpanded = !wasteMenuExpanded }
                        ) {
                            val currentWasteLabel = wasteOptions.find { it.first == recipe.wastePercentage }?.second
                                ?: "${recipe.wastePercentage}%"

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .clickable { wasteMenuExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = currentWasteLabel,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1
                                    )
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = wasteMenuExpanded)
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = wasteMenuExpanded,
                                onDismissRequest = { wasteMenuExpanded = false }
                            ) {
                                wasteOptions.forEach { (pct, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            onUpdateRecipePricing(null, pct, null, null, null, null, null, null)
                                            wasteMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Row 2 of financial cards: Costo Total & Precio de Venta
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Costo Total
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Costo total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(financials.totalCost, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (financials.wasteCost > 0) {
                            Text(
                                text = "+${formatCurrency(financials.wasteCost, currencySymbol)} por merma",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Precio de Venta Editable
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Precio de venta (${currencySymbol})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = salePriceText,
                            onValueChange = { input ->
                                salePriceText = input
                                input.toDoubleOrNull()?.let { p ->
                                    if (p >= 0) onUpdateRecipePricing(p, null, null, null, null, null, null, null)
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sale_price_input"),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Quick margin target pills: "Sugerir precio para margen:"
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Fijar precio por margen deseado:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(30.0, 40.0, 50.0, 60.0).forEach { marginTarget ->
                            OutlinedButton(
                                onClick = { onApplyTargetMargin(marginTarget) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("${marginTarget.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Row 3 of financial cards: IGV / Impuesto & Comisión de Cobro (Totalmente Flexible)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Impuesto / IGV
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "IGV / Impuesto",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${recipe.taxPercentage.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(financials.taxAmount, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (recipe.isTaxIncludedInPrice) "Incluido en precio web" else "No incluido",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Comisión de Cobro / Pasarela / Canal (Universal y Configurable)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isEditingPricingSettings = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Comisión de cobro",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Configurar método",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(financials.gatewayCommissionAmount, currencySymbol),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (financials.hasGatewayCommission) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (financials.hasGatewayCommission) recipe.gatewayName else "Sin comisión (Efectivo)",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            color = if (financials.hasGatewayCommission) PrimaryGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Hero Card 1: Ganancia Real Por Ración
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profit_hero_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (financials.realNetProfit >= 0) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else ExpenseRed.copy(alpha = 0.15f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Ganancia real por ración",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = formatCurrency(financials.realNetProfit, currencySymbol),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = if (financials.realNetProfit >= 0) ProfitGreen else ExpenseRed
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic transparent formula breakdown:
                    // If commission exists: S/ 26.00 – S/ 3.97 – S/ 3.50 – S/ 7.92
                    // If no commission:     S/ 26.00 – S/ 3.97 – S/ 7.92
                    val formulaText = buildString {
                        append(String.format(Locale.US, "%s %.2f", currencySymbol, financials.salePrice))
                        if (financials.taxAmount > 0) {
                            append(String.format(Locale.US, " – %s %.2f (IGV)", currencySymbol, financials.taxAmount))
                        }
                        if (financials.hasGatewayCommission) {
                            append(String.format(Locale.US, " – %s %.2f (%s)", currencySymbol, financials.gatewayCommissionAmount, recipe.gatewayName))
                        }
                        append(String.format(Locale.US, " – %s %.2f (Costo)", currencySymbol, financials.totalCost))
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formulaText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Hero Card 2: Margen de Ganancia (%)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("margin_hero_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Margen de ganancia neto",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ProfitabilityBadge(
                            level = financials.profitabilityLevel,
                            marginPct = financials.marginPercentage
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatPercent(financials.marginPercentage),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = when {
                            financials.marginPercentage >= 40.0 -> ProfitGreen
                            financials.marginPercentage >= 25.0 -> Color(0xFF38BDF8)
                            financials.marginPercentage >= 10.0 -> PrimaryGold
                            else -> ExpenseRed
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Margen sobre costo (Markup): ${formatPercent(financials.markupPercentage)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Actions: Share & Config
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Sheet button
                Button(
                    onClick = {
                        val reportText = buildShareReport(recipe, financials, items, currencySymbol)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartir Costeo"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir")
                }

                // Copy to Clipboard button
                OutlinedButton(
                    onClick = {
                        val reportText = buildShareReport(recipe, financials, items, currencySymbol)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ficha de Costos", reportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Ficha copiada al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar")
                }
            }
        }

        // Edit Advanced Settings button
        item {
            TextButton(
                onClick = { isEditingPricingSettings = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Configurar método de cobro, impuestos y mano de obra")
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal to customize Taxes, Gateway commissions and Labor costs
    if (isEditingPricingSettings) {
        TaxAndCommissionDialog(
            currentTaxPct = recipe.taxPercentage,
            isTaxIncluded = recipe.isTaxIncludedInPrice,
            currentGatewayName = recipe.gatewayName,
            currentGatewayFixed = recipe.gatewayFixedCommission,
            currentGatewayPct = recipe.gatewayCommissionPercentage,
            currentLaborCost = recipe.laborCost,
            currencySymbol = currencySymbol,
            onDismiss = { isEditingPricingSettings = false },
            onSave = { taxPct, taxInc, gName, gFixed, gPct, labor ->
                onUpdateRecipePricing(null, null, taxPct, taxInc, gName, gFixed, gPct, labor)
                isEditingPricingSettings = false
            }
        )
    }
}

private fun buildShareReport(
    recipe: Recipe,
    financials: RecipeFinancials,
    items: List<RecipeItemDetail>,
    currencySymbol: String
): String {
    val builder = StringBuilder()
    builder.appendLine("📊 FICHA DE COSTOS Y RENTABILIDAD")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("📌 Producto: ${recipe.name}")
    if (recipe.description.isNotBlank()) {
        builder.appendLine("📝 Detalle: ${recipe.description}")
    }
    builder.appendLine()
    builder.appendLine("🛒 ELEMENTOS Y CONSUMO:")
    items.forEach { item ->
        val cat = item.catalogItem
        builder.appendLine(" • ${cat.name}: ${item.recipeItem.quantity} ${cat.unit} = ${formatCurrency(item.subtotalCost, currencySymbol)}")
    }
    builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("🔹 Costo Primo Directo: ${formatCurrency(financials.directMaterialsCost, currencySymbol)}")
    if (financials.wastePercentage > 0) {
        builder.appendLine("🔹 Merma (${financials.wastePercentage}%): ${formatCurrency(financials.wasteCost, currencySymbol)}")
    }
    if (financials.laborCost > 0) {
        builder.appendLine("🔹 Mano de obra: ${formatCurrency(financials.laborCost, currencySymbol)}")
    }
    builder.appendLine("💰 Costo Total de Producción: ${formatCurrency(financials.totalCost, currencySymbol)}")
    builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("🏷️ Precio de Venta: ${formatCurrency(financials.salePrice, currencySymbol)}")
    if (financials.taxAmount > 0) {
        builder.appendLine("🏛️ Impuesto (${recipe.taxPercentage}%): -${formatCurrency(financials.taxAmount, currencySymbol)}")
    }
    if (financials.hasGatewayCommission) {
        builder.appendLine("💳 Comisión de cobro (${recipe.gatewayName}): -${formatCurrency(financials.gatewayCommissionAmount, currencySymbol)}")
    } else {
        builder.appendLine("💳 Método de cobro: Sin comisión (Venta directa / Efectivo)")
    }
    builder.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    builder.appendLine("✅ GANANCIA REAL: ${formatCurrency(financials.realNetProfit, currencySymbol)}")
    builder.appendLine("📈 MARGEN NETO: ${formatPercent(financials.marginPercentage)}")
    builder.appendLine("📊 MARGEN SOBRE COSTO: ${formatPercent(financials.markupPercentage)}")
    return builder.toString()
}

@Composable
fun TaxAndCommissionDialog(
    currentTaxPct: Double,
    isTaxIncluded: Boolean,
    currentGatewayName: String,
    currentGatewayFixed: Double,
    currentGatewayPct: Double,
    currentLaborCost: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (taxPct: Double, isTaxIncluded: Boolean, gatewayName: String, gatewayFixed: Double, gatewayPct: Double, labor: Double) -> Unit
) {
    var taxPctText by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentTaxPct)) }
    var taxIncluded by remember { mutableStateOf(isTaxIncluded) }

    var gatewayName by remember { mutableStateOf(currentGatewayName) }
    var gatewayFixedText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentGatewayFixed)) }
    var gatewayPctText by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentGatewayPct)) }
    var laborText by remember { mutableStateOf(String.format(Locale.US, "%.2f", currentLaborCost)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Parámetros de Cobro e Impuestos", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Section 1: Presets for Payment Method / Commission
                Text(
                    text = "Método de cobro / pasarela",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = gatewayFixedText == "0.00" && (gatewayPctText == "0.0" || gatewayPctText == "0"),
                        onClick = {
                            gatewayName = "Sin comisión (Efectivo)"
                            gatewayFixedText = "0.00"
                            gatewayPctText = "0.0"
                        },
                        label = { Text("Efectivo (0%)", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = gatewayPctText == "3.5" && gatewayFixedText == "0.00",
                        onClick = {
                            gatewayName = "POS / Tarjeta"
                            gatewayFixedText = "0.00"
                            gatewayPctText = "3.5"
                        },
                        label = { Text("POS (3.5%)", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = gatewayPctText == "20.0" || gatewayPctText == "20",
                        onClick = {
                            gatewayName = "App Delivery"
                            gatewayFixedText = "0.00"
                            gatewayPctText = "20.0"
                        },
                        label = { Text("Delivery (20%)", fontSize = 11.sp) }
                    )
                }

                // Custom Gateway Name
                OutlinedTextField(
                    value = gatewayName,
                    onValueChange = { gatewayName = it },
                    label = { Text("Nombre del canal / pasarela") },
                    placeholder = { Text("Ej: Efectivo, POS, Culqi, Mercado Pago...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gateway %
                    OutlinedTextField(
                        value = gatewayPctText,
                        onValueChange = { gatewayPctText = it },
                        label = { Text("Comisión (%)") },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Gateway fixed
                    OutlinedTextField(
                        value = gatewayFixedText,
                        onValueChange = { gatewayFixedText = it },
                        label = { Text("Fijo ($currencySymbol)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Section 2: Tax (IGV)
                Text(
                    text = "Impuestos (IGV / IVA)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = taxPctText,
                    onValueChange = { taxPctText = it },
                    label = { Text("Tasa de impuesto (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Precio carta ya incluye impuesto",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = taxIncluded,
                        onCheckedChange = { taxIncluded = it }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Section 3: Labor cost
                OutlinedTextField(
                    value = laborText,
                    onValueChange = { laborText = it },
                    label = { Text("Mano de obra directa ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tax = taxPctText.toDoubleOrNull() ?: currentTaxPct
                    val gFixed = gatewayFixedText.toDoubleOrNull() ?: currentGatewayFixed
                    val gPct = gatewayPctText.toDoubleOrNull() ?: currentGatewayPct
                    val labor = laborText.toDoubleOrNull() ?: currentLaborCost
                    val name = gatewayName.ifBlank { "Sin comisión" }
                    onSave(tax, taxIncluded, name, gFixed, gPct, labor)
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
