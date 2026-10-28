package com.group.pockit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Shopping, Food, Allowance... Shared by all users and created once by AppDatabase
 * the first time the app runs. Each category is either INCOME or EXPENSE.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: RecordType,
    /** Frontend maps this key to a drawable, e.g. "shopping", "food", "health". */
    val iconKey: String
)
