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
    private var allTransactions = listOf(
        Transaction("1", "Shopping", "M-Pesa", "26 Oct", 3500.0, "Expense"),
        Transaction("2", "Bills", "Bank", "25 Oct", 2500.0, "Expense"),
        Transaction("3", "Entertainment", "M-Pesa", "23 Oct", 1200.0, "Expense"),
        Transaction("4", "Salary", "Bank", "20 Oct", 45000.0, "Income"),
        Transaction("5", "Gift", "Cash", "15 Oct", 2000.0, "Income")
    )

    init {
        loadData()
    }

    fun setType(type: String) {
        currentType = type
        loadData()
    }

    private fun loadData() {
        val filteredList = allTransactions.filter { it.type == currentType }

        val dummyOverview = if (currentType == "Expense") {
            Overview(remaining = 650.0, spent = 10850.0, limit = 11500.0, over = false)
        } else {
            Overview(remaining = 47000.0, spent = 47000.0, limit = 50000.0, over = false)
        }

        _state.value = DashboardState(
            overview = dummyOverview,
            recent = filteredList
        )
    }
}