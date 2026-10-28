package com.group.pockit.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.group.pockit.data.PockitRepository
import com.group.pockit.data.model.BudgetOverview
import com.group.pockit.util.DateRanges
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Budget screen and the "Set budget" screen.
 *
 *     vm.overview.observe(viewLifecycleOwner) { o ->
 *         binding.left.text = Money.ksh(o.remaining)          // "Left to spend this month"
 *         binding.bar.progress = o.usedPercent                 // "83% used"
 *         adapter.submitList(o.categories)                     // Airtime, Shopping, ... Entertainment
 *     }
 */
class BudgetViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PockitRepository.get(app)

    private val month = MutableStateFlow(YearMonth.now())

    /** "Oct 2026" between the < > arrows. */
    val monthLabel: LiveData<String> = month.map { DateRanges.monthRange(it).label }.asLiveData()

    /**
     * The purple card (totalLimit, totalSpent, remaining, usedPercent) and every expense category
     * (limitAmount is null for those without a budget yet, show the "Set budget" button for them).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val overview: LiveData<BudgetOverview> = month.flatMapLatest { repo.observeBudget(it) }.asLiveData()

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    /**
     * "Save budget" button. onResult(false) means the limit was 0 or negative and nothing was saved.
     * Setting a limit for a category that already has one replaces it.
     */
    fun setBudget(categoryId: Long, limit: Double, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.setBudget(month.value, categoryId, limit)) }
    }

    /** Removes the limit of a category (it goes back to "no limit"). */
    fun removeBudget(categoryId: Long) {
        viewModelScope.launch { repo.removeBudget(month.value, categoryId) }
    }

    /** Creates a new custom expense category and sets its budget for the current month. */
    fun addCategory(name: String, limit: Double, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.addBudgetCategory(name, limit)) }
    }
}
