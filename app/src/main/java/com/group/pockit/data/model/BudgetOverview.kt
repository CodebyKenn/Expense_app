package com.group.pockit.data.model

/**
 * Everything the Budget screen needs for one month: the purple card at the top
 * ("Left to spend this month") and the list of categories underneath.
 *
 * Only categories that HAVE a limit count towards the totals, so an unbudgeted
 * category (like Entertainment in the design) does not eat into "left to spend".
 */
data class BudgetOverview(val categories: List<BudgetStatus>) {

    private val budgeted: List<BudgetStatus> get() = categories.filter { it.hasLimit }

    /** "Ksh 4,000 limit" */
    val totalLimit: Double get() = budgeted.sumOf { it.limitAmount ?: 0.0 }

    /** "Spent Ksh 3,300" */
    val totalSpent: Double get() = budgeted.sumOf { it.spent }

    /** "Left to spend this month". Negative when the total is exceeded. */
    val remaining: Double get() = totalLimit - totalSpent

    /** "83% used" */
    val usedPercent: Int
        get() = if (totalLimit > 0.0) Math.round(totalSpent / totalLimit * 100).toInt() else 0

    val isOver: Boolean get() = totalSpent > totalLimit

    /**
     * "Your total budget becomes Ksh 5,000" while the user is typing a new limit for one category.
     * The old limit of that category (if any) is replaced by the new one.
     */
    fun totalLimitIfSet(categoryId: Long, newLimit: Double): Double {
        val old = categories.firstOrNull { it.categoryId == categoryId }?.limitAmount ?: 0.0
        return totalLimit - old + newLimit
    }
}
