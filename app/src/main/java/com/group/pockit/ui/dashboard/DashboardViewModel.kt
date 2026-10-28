package com.group.pockit.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Overview(
    val remaining: Double,
    val spent: Double,
    val limit: Double,
    val over: Boolean
)

data class Transaction(
    val id: String,
    val category: String,
    val account: String,
    val date: String,
    val amount: Double,
    val type: String
)

data class DashboardState(
    val overview: Overview,
    val recent: List<Transaction>
)

class DashboardViewModel : ViewModel() {

    private val _state = MutableLiveData<DashboardState>()
    val state: LiveData<DashboardState> = _state

    private var currentType = "Expense"

    // Monthly budget limit (sample value until the database is connected)
    private val monthlyLimit = 11500.0

    // Sample transactions: same numbers as the Summary and Budget screens
    private val allTransactions = listOf(
        Transaction("1", "Shopping", "M-Pesa", "26 Oct", 3500.0, "Expense"),
        Transaction("2", "Food", "Cash", "25 Oct", 3450.0, "Expense"),
        Transaction("3", "Bills", "Bank", "25 Oct", 2500.0, "Expense"),
        Transaction("4", "Entertainment", "M-Pesa", "23 Oct", 1200.0, "Expense"),
        Transaction("5", "Transport", "M-Pesa", "22 Oct", 200.0, "Expense"),
        Transaction("6", "Salary", "Bank", "20 Oct", 45000.0, "Income"),
        Transaction("7", "Gift", "Cash", "15 Oct", 2000.0, "Income")
    )

    init {
        loadData()
    }

    fun setType(type: String) {
        currentType = type
        loadData()
    }

    private fun loadData() {
        val totalSpent = allTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val totalIncome = allTransactions.filter { it.type == "Income" }.sumOf { it.amount }

        // Numbers are now calculated from the transactions, not typed in
        val overview = if (currentType == "Expense") {
            Overview(
                remaining = monthlyLimit - totalSpent,
                spent = totalSpent,
                limit = monthlyLimit,
                over = totalSpent > monthlyLimit
            )
        } else {
            // Income view: how much of this month's income is left after spending
            Overview(
                remaining = totalIncome - totalSpent,
                spent = totalSpent,
                limit = totalIncome,
                over = totalSpent > totalIncome
            )
        }

        _state.value = DashboardState(
            overview = overview,
            recent = allTransactions.filter { it.type == currentType }
        )
    }
}