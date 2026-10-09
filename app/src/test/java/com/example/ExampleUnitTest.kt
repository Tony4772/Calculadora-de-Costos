package com.example

import com.example.data.model.CatalogItem
import com.example.data.model.CostCalculatorEngine
import com.example.data.model.Recipe
import com.example.data.model.RecipeItem
import com.example.data.model.RecipeItemDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCatalogUnitCost() {
        val queso = CatalogItem(
            name = "Queso",
            purchasePrice = 20.00,
            purchaseQuantity = 1000.0,
            unit = "Gramos"
        )
        // 20 / 1000 = 0.020
        assertEquals(0.020, queso.unitCost, 0.0001)

        val caja = CatalogItem(
            name = "Caja",
            purchasePrice = 500.00,
            purchaseQuantity = 100.0,
            unit = "Unidades"
        )
        // 500 / 100 = 5.00
        assertEquals(5.00, caja.unitCost, 0.0001)
    }

    @Test
    fun testZeroCommissionCashSale() {
        // Test cash / direct sale without payment gateway
        val recipe = Recipe(
            name = "Venta Directa Efectivo",
            salePrice = 25.00,
            taxPercentage = 0.0, // Negocio pequeño sin IGV o exento
            isTaxIncludedInPrice = false,
            gatewayName = "Sin comisión (Efectivo)",
            gatewayFixedCommission = 0.0,
            gatewayCommissionPercentage = 0.0,
            wastePercentage = 0.0
        )

        val items = listOf(
            RecipeItemDetail(
                recipeItem = RecipeItem(recipeId = 1, catalogItemId = 1, quantity = 100.0),
                catalogItem = CatalogItem(name = "Insumo A", purchasePrice = 10.0, purchaseQuantity = 100.0, unit = "g")
            )
        )

        val fin = CostCalculatorEngine.calculate(recipe, items)
        assertEquals(10.00, fin.directMaterialsCost, 0.01)
        assertEquals(0.00, fin.gatewayCommissionAmount, 0.0001)
        assertFalse(fin.hasGatewayCommission)
        // Profit = 25.00 - 10.00 = 15.00
        assertEquals(15.00, fin.realNetProfit, 0.01)
        // Margin = 15 / 25 = 60%
        assertEquals(60.0, fin.marginPercentage, 0.01)
    }

    @Test
    fun testCostAndMarginCalculationWithGateway() {
        val recipe = Recipe(
            name = "Producto con Pasarela",
            salePrice = 26.00,
            taxPercentage = 18.0,
            isTaxIncludedInPrice = true,
            gatewayName = "Pasarela de Pago",
            gatewayFixedCommission = 3.50,
            wastePercentage = 0.0
        )

        val items = listOf(
            RecipeItemDetail(
                recipeItem = RecipeItem(recipeId = 1, catalogItemId = 1, quantity = 60.0),
                catalogItem = CatalogItem(name = "Materia Prima Principal", purchasePrice = 20.0, purchaseQuantity = 1000.0, unit = "g")
            ),
            RecipeItemDetail(
                recipeItem = RecipeItem(recipeId = 1, catalogItemId = 2, quantity = 6.0),
                catalogItem = CatalogItem(name = "Material Secundario", purchasePrice = 4.5, purchaseQuantity = 100.0, unit = "u")
            ),
            RecipeItemDetail(
                recipeItem = RecipeItem(recipeId = 1, catalogItemId = 3, quantity = 1.0),
                catalogItem = CatalogItem(name = "Empaque / Envase", purchasePrice = 5.0, purchaseQuantity = 1.0, unit = "u")
            )
        )

        val fin = CostCalculatorEngine.calculate(recipe, items)

        // Direct cost: 1.20 + 0.27 + 5.00 = 6.47
        assertEquals(6.47, fin.directMaterialsCost, 0.01)

        // Tax calculation for 26.00 with 18% IGV included:
        // 26 - (26 / 1.18) = 26 - 22.03389 = 3.9661 (~3.97)
        assertEquals(3.97, fin.taxAmount, 0.02)

        // Real profit = 26.00 - tax (3.97) - culqi (3.50) - cost (6.47) = 12.06
        assertEquals(26.00 - fin.taxAmount - 3.50 - 6.47, fin.realNetProfit, 0.01)
        assertTrue(fin.marginPercentage > 0)
    }
}
