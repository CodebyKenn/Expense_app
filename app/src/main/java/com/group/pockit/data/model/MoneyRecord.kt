package com.group.pockit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row on the "Enter record" screen: an income, an expense or a transfer.
 *
 *  - INCOME / EXPENSE: accountId + categoryId are set, toAccountId is null.
 *  - TRANSFER: accountId (from) + toAccountId (to) are set, categoryId is null.
 *
 * amount is always positive. The type decides whether it adds or subtracts.
 * timestamp is epoch milliseconds (date + time picked on the screen combined).
 */
@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId", "timestamp"]),
        Index("accountId"),
        Index("toAccountId"),
        Index("categoryId")
    ]
)
data class MoneyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val type: RecordType,
    val amount: Double,
    val accountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val timestamp: Long
)
