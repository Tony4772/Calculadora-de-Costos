package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "catalog_items")
data class CatalogItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = CATEGORY_INSUMO, // "Insumo", "Empaque", "Otro"
    val purchasePrice: Double,
    val purchaseQuantity: Double,
    val unit: String, // "Gramos", "Mililitros", "Láminas / Unidades", "Onzas", "Kilogramos", "Litros"
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val unitCost: Double
        get() = if (purchaseQuantity > 0.0) purchasePrice / purchaseQuantity else 0.0

    fun formatUnitCost(currencySymbol: String = "S/"): String {
        return String.format(Locale.US, "%s %.3f por %s", currencySymbol, unitCost, unit.lowercase())
    }

    companion object {
        const val CATEGORY_INSUMO = "Insumo"
        const val CATEGORY_EMPAQUE = "Empaque"
        const val CATEGORY_OTRO = "Otro"

        val DEFAULT_UNITS = listOf(
            "Gramos",
            "Mililitros",
            "Láminas / Unidades",
            "Onzas",
            "Kilogramos",
            "Litros",
            "Piezas",
            "Porción"
        )
    }
}
