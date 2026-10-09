package com.example.data.model

import java.util.Locale

data class RecipeItemDetail(
    val recipeItem: RecipeItem,
    val catalogItem: CatalogItem
) {
    val subtotalCost: Double
        get() = recipeItem.quantity * catalogItem.unitCost

    fun formatFormula(currencySymbol: String = "S/"): String {
        return String.format(
            Locale.US,
            "%.2f × %s %.3f = %s %.2f",
            recipeItem.quantity,
            currencySymbol,
            catalogItem.unitCost,
            currencySymbol,
            subtotalCost
        )
    }
}

enum class ProfitabilityLevel(val label: String) {
    EXCELLENT("Excelente Rentabilidad (≥ 40%)"),
    HEALTHY("Margen Saludable (25% - 40%)"),
    TIGHT("Margen Ajustado (10% - 25%)"),
    CRITICAL("Margen Crítico (< 10%)"),
    LOSS("En Pérdida (< 0%)")
}

data class RecipeFinancials(
    val directMaterialsCost: Double, // Costo primo directo
    val ingredientsCost: Double,
    val packagingCost: Double,
    val otherCost: Double,
    val wastePercentage: Double,
    val wasteCost: Double, // Merma en dinero
    val laborCost: Double,
    val totalCost: Double, // Costo total = primo + merma + mano obra
    val salePrice: Double,
    val taxPercentage: Double,
    val isTaxIncluded: Boolean,
    val taxAmount: Double, // IGV / IVA
    val gatewayName: String, // "Sin comisión (Efectivo)", "POS / Tarjeta", etc.
    val gatewayCommissionAmount: Double, // Comisión calculada
    val hasGatewayCommission: Boolean,
    val realNetProfit: Double, // Ganancia real
    val marginPercentage: Double, // Margen de ganancia neto %
    val markupPercentage: Double, // Margen sobre costo %
    val profitabilityLevel: ProfitabilityLevel
)

object CostCalculatorEngine {
    fun calculate(recipe: Recipe, items: List<RecipeItemDetail>): RecipeFinancials {
        var directCost = 0.0
        var ingCost = 0.0
        var packCost = 0.0
        var othCost = 0.0

        for (item in items) {
            val cost = item.subtotalCost
            directCost += cost
            when (item.catalogItem.category) {
                CatalogItem.CATEGORY_INSUMO -> ingCost += cost
                CatalogItem.CATEGORY_EMPAQUE -> packCost += cost
                else -> othCost += cost
            }
        }

        val wasteCost = directCost * (recipe.wastePercentage / 100.0)
        val totalCost = directCost + wasteCost + recipe.laborCost

        val salePrice = recipe.salePrice

        // Calculation of Tax (e.g., IGV 18% in Peru):
        val taxAmount = if (recipe.isTaxIncludedInPrice && recipe.taxPercentage > 0.0) {
            salePrice - (salePrice / (1.0 + recipe.taxPercentage / 100.0))
        } else if (!recipe.isTaxIncludedInPrice && recipe.taxPercentage > 0.0) {
            salePrice * (recipe.taxPercentage / 100.0)
        } else {
            0.0
        }

        // Commission calculation (0 if no gateway or cash):
        val gatewayCommission = (salePrice * (recipe.gatewayCommissionPercentage / 100.0)) + recipe.gatewayFixedCommission
        val hasCommission = gatewayCommission > 0.0001

        // Real net profit: SalePrice - Tax - GatewayCommission - TotalCost
        val realProfit = if (salePrice > 0.0) {
            salePrice - taxAmount - gatewayCommission - totalCost
        } else {
            -totalCost
        }

        // Margin % on sale price
        val marginPercentage = if (salePrice > 0.0) {
            (realProfit / salePrice) * 100.0
        } else {
            0.0
        }

        // Markup % on production cost
        val markupPercentage = if (totalCost > 0.0) {
            (realProfit / totalCost) * 100.0
        } else {
            0.0
        }

        val level = when {
            realProfit < 0.0 -> ProfitabilityLevel.LOSS
            marginPercentage >= 40.0 -> ProfitabilityLevel.EXCELLENT
            marginPercentage >= 25.0 -> ProfitabilityLevel.HEALTHY
            marginPercentage >= 10.0 -> ProfitabilityLevel.TIGHT
            else -> ProfitabilityLevel.CRITICAL
        }

        return RecipeFinancials(
            directMaterialsCost = directCost,
            ingredientsCost = ingCost,
            packagingCost = packCost,
            otherCost = othCost,
            wastePercentage = recipe.wastePercentage,
            wasteCost = wasteCost,
            laborCost = recipe.laborCost,
            totalCost = totalCost,
            salePrice = salePrice,
            taxPercentage = recipe.taxPercentage,
            isTaxIncluded = recipe.isTaxIncludedInPrice,
            taxAmount = taxAmount,
            gatewayName = recipe.gatewayName,
            gatewayCommissionAmount = gatewayCommission,
            hasGatewayCommission = hasCommission,
            realNetProfit = realProfit,
            marginPercentage = marginPercentage,
            markupPercentage = markupPercentage,
            profitabilityLevel = level
        )
    }

    /**
     * Helper to compute suggested sale price for a target profit margin (e.g. 40%)
     */
    fun suggestPriceForMargin(
        recipe: Recipe,
        totalCost: Double,
        targetMarginPct: Double
    ): Double {
        val targetMarginFrac = (targetMarginPct / 100.0).coerceIn(0.0, 0.90)
        val taxFrac = if (recipe.isTaxIncludedInPrice && recipe.taxPercentage > 0.0) {
            1.0 - (1.0 / (1.0 + recipe.taxPercentage / 100.0))
        } else 0.0
        val commFrac = recipe.gatewayCommissionPercentage / 100.0

        val denominator = 1.0 - targetMarginFrac - taxFrac - commFrac
        return if (denominator > 0.05) {
            (totalCost + recipe.gatewayFixedCommission) / denominator
        } else {
            totalCost * 2.0
        }
    }
}
