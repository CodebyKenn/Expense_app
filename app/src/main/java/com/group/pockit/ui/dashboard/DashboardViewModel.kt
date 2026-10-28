package com.group.pockit.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.group.pockit.data.PockitRepository
import com.group.pockit.data.Session
import com.group.pockit.data.model.BudgetOverview
import com.group.pockit.data.model.RecordItem
import com.group.pockit.data.model.RecordType
import com.group.pockit.util.DateRanges
import com.group.pockit.util.Period
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PockitRepository.get(app)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())

    private val _state = MediatorLiveData<DashboardState>()
    val state: LiveData<DashboardState> = _state

    private var currentType = "Expense"
    private var currentPeriod = Period.MONTH

    val greetingName: String get() = Session.displayName

    init {
        viewModelScope.launch {
            repo.ensureDefaultAccounts()
        }
        setupLiveDataSources()
    }

    fun setType(type: String) {
        currentType = type
        setupLiveDataSources()
    }

    fun setPeriod(period: Period) {
        currentPeriod = period
        setupLiveDataSources()
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repo.forceSeedDemoData()
        }
    }

    private fun setupLiveDataSources() {
        val range = DateRanges.of(currentPeriod, LocalDate.now())
        val incomeLive = repo.observeTotal(RecordType.INCOME, range).asLiveData()
        val expenseLive = repo.observeTotal(RecordType.EXPENSE, range).asLiveData()
        val recentLive = repo.observeRecent(50).asLiveData()
        val budgetLive = repo.observeBudget(YearMonth.now()).asLiveData()

        _state.removeSource(incomeLive)
        _state.removeSource(expenseLive)
        _state.removeSource(recentLive)
        _state.removeSource(budgetLive)

        val update = {
            val income = incomeLive.value ?: 0.0
            val expense = expenseLive.value ?: 0.0
            val items = recentLive.value ?: emptyList()
            val budget = budgetLive.value
            updateState(income, expense, items, budget)
        }

        _state.addSource(incomeLive) { update() }
        _state.addSource(expenseLive) { update() }
        _state.addSource(recentLive) { update() }
        _state.addSource(budgetLive) { update() }
    }

    private fun updateState(
        totalIncome: Double,
        totalSpent: Double,
        items: List<RecordItem>,
        budget: BudgetOverview?
    ) {
        val budgetLimit = budget?.totalLimit ?: 0.0

        val overview = if (currentType == "Expense") {
            val limit = if (budgetLimit > 0) budgetLimit else 0.0
            val remaining = if (limit > 0) limit - totalSpent else 0.0
            Overview(
                remaining = remaining,
                spent = totalSpent,
                limit = limit,
                over = limit > 0 && totalSpent > limit
            )
        } else {
            Overview(
                remaining = totalIncome - totalSpent,
                spent = totalSpent,
                limit = totalIncome,
                over = totalSpent > totalIncome && totalIncome > 0
            )
        }

        val filteredItems = items.filter { item ->
            if (currentType == "Expense") item.isExpense else item.isIncome
        }.map { item ->
            val dateStr = try {
                Instant.ofEpochMilli(item.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(dateFormatter)
            } catch (_: Exception) {
                "Today"
            }
            Transaction(
                id = item.id.toString(),
                category = item.categoryName ?: item.toAccountName ?: "General",
                account = item.accountName,
                date = dateStr,
                amount = item.amount,
                type = if (item.isIncome) "Income" else if (item.isExpense) "Expense" else "Transfer"
            )
        }

        _state.value = DashboardState(
            overview = overview,
            recent = filteredItems
        )
    }
}
