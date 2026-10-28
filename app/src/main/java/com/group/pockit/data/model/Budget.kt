package com.group.pockit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A monthly spending limit for one EXPENSE category, e.g. Food, "2026-10", 1500.
 * There is no separate "total budget": the total on the Budget screen is the sum
 * of the category limits (that is how the design shows it).
 * One row per user + month + category (enforced by the unique index).
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "month", "categoryId"], unique = true), Index("categoryId")]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    /** Format "yyyy-MM", for example "2026-10". YearMonth.toString() gives exactly this. */
    val month: String,
    val categoryId: Long,
    val limitAmount: Double
)
