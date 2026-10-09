package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val portions: Int = 1,
    val salePrice: Double = 0.0,
    val wastePercentage: Double = 0.0, // Merma: 0%, 5%, 8%, etc.
    val taxPercentage: Double = 18.0, // IGV / IVA %
    val isTaxIncludedInPrice: Boolean = true, // Precio carta incluye IGV
    val gatewayName: String = "Sin comisión (Efectivo)", // Nombre descriptivo del método de cobro
    val gatewayCommissionPercentage: Double = 0.0, // % pasarela / POS / delivery
    val gatewayFixedCommission: Double = 0.0, // Monto fijo pasarela (opcional)
    val laborCost: Double = 0.0, // Mano de obra o costos indirectos
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
