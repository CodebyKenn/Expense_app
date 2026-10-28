package com.group.pockit.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A place where money lives: M-Pesa, Cash, Bank, or one the user adds.
 * The balance is NOT stored here. It is calculated from the records
 * (see AccountDao.observeBalances), so it can never get out of sync.
 */
@Entity(tableName = "accounts", indices = [Index("userId")])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    /** Frontend maps this key to a drawable, e.g. "mpesa", "cash", "bank", "wallet". */
    val iconKey: String = "wallet",
    val openingBalance: Double = 0.0
)
