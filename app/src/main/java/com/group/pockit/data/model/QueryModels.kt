package com.group.pockit.data.model

/*
 * Read-only shapes that the DAO queries return. They are not tables.
 * The screens use these directly, e.g. one row of the "Recent transaction" list.
 */

/** One row of the Accounts screen and the account chips on the Dashboard. */
data class AccountBalance(
    val id: Long,
    val name: String,
    val iconKey: String,
    val balance: Double
)

/**
 * One row of any transaction list (Dashboard "Recent transaction", search results).
 * categoryName / categoryIconKey are null for transfers; toAccountName is only set for transfers.
 */
data class RecordItem(
    val id: Long,
    val type: RecordType,
    val amount: Double,
    val note: String,
    val timestamp: Long,
    val accountId: Long,
    val accountName: String,
    val toAccountName: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryIconKey: String?
) {
    val isIncome: Boolean get() = type == RecordType.INCOME
    val isExpense: Boolean get() = type == RecordType.EXPENSE
    val isTransfer: Boolean get() = type == RecordType.TRANSFER
}

/** Raw total for one category in a period (straight from the database). */
data class CategoryTotal(
    val categoryId: Long,
    val name: String,
    val iconKey: String,
    val total: Double
)

/** One slice of the donut chart plus its row underneath: category, amount and percentage. */
data class CategoryShare(
    val categoryId: Long,
    val name: String,
    val iconKey: String,
    val total: Double,
    val percent: Int
)

/** One account on the "Accounts" analysis tab: income bar, expense bar and the net. */
data class AccountFlow(
    val accountId: Long,
    val name: String,
    val income: Double,
    val expense: Double
) {
    /** Positive = "Net + Ksh 5,200" (green), negative = "Net - Ksh 500" (red). */
    val net: Double get() = income - expense
}

/** One category row on the Budget screen. limitAmount is null when no budget is set yet. */
data class BudgetStatus(
    val categoryId: Long,
    val name: String,
    val iconKey: String,
    val limitAmount: Double?,
    val spent: Double
) {
    val hasLimit: Boolean get() = limitAmount != null

    /** Money left. Negative means over budget ("Ksh 150 over"). 0 when there is no limit. */
    val remaining: Double get() = (limitAmount ?: 0.0) - spent

    val isOver: Boolean get() = limitAmount != null && spent > limitAmount

    /** 0.0 to 1.0, for the progress bar. */
    val usedFraction: Double
        get() = if (limitAmount != null && limitAmount > 0.0) (spent / limitAmount).coerceIn(0.0, 1.0) else 0.0

    /** 80% or more used but not over yet. Use it for the yellow bar. */
    val isNearLimit: Boolean get() = hasLimit && !isOver && usedFraction >= 0.8
}
