package com.group.pockit.ui.analysis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.group.pockit.data.PockitRepository
import com.group.pockit.data.model.AccountFlow
import com.group.pockit.data.model.CategoryShare
import com.group.pockit.data.model.RecordType
import com.group.pockit.util.DateRanges
import com.group.pockit.util.Period
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * "Analysis" screens: the Day / Week / Month switch, the "< Oct 19 - Oct 25 >" arrows,
 * the EXPENSE / INCOME / TOTAL line and the three tabs (Expenses, Income, Accounts).
 * One ViewModel serves all three tabs, because they share the same period.
 *
 * Tabs: Expenses -> expenseBreakdown, Income -> incomeBreakdown, Accounts -> accountFlows.
 */
class AnalysisViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PockitRepository.get(app)

    private data class Window(val period: Period, val anchor: LocalDate)

    private val window = MutableStateFlow(Window(Period.WEEK, LocalDate.now()))
    private val range = window.map { DateRanges.of(it.period, it.anchor) }

    /** Which of Day / Week / Month is selected. */
    val period: LiveData<Period> = window.map { it.period }.asLiveData()

    /** "Oct 19 - Oct 25" */
    val rangeLabel: LiveData<String> = range.map { it.label }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val expenseFlow = range.flatMapLatest { repo.observeTotal(RecordType.EXPENSE, it) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val incomeFlow = range.flatMapLatest { repo.observeTotal(RecordType.INCOME, it) }

    /** EXPENSE  Ksh 17,800 */
    val expenseTotal: LiveData<Double> = expenseFlow.asLiveData()

    /** INCOME  Ksh 23,000 */
    val incomeTotal: LiveData<Double> = incomeFlow.asLiveData()

    /** TOTAL  Ksh 5,200  (income minus expense, can be negative) */
    val netTotal: LiveData<Double> = combine(incomeFlow, expenseFlow) { income, expense -> income - expense }.asLiveData()

    /** Expenses tab: donut + rows. Each item has name, iconKey, total, percent. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenseBreakdown: LiveData<List<CategoryShare>> =
        range.flatMapLatest { repo.observeBreakdown(RecordType.EXPENSE, it) }.asLiveData()

    /** Income tab: donut + rows. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val incomeBreakdown: LiveData<List<CategoryShare>> =
        range.flatMapLatest { repo.observeBreakdown(RecordType.INCOME, it) }.asLiveData()

    /** Accounts tab: one entry per account with income, expense and net. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val accountFlows: LiveData<List<AccountFlow>> =
        range.flatMapLatest { repo.observeAccountFlows(it) }.asLiveData()

    // ----- controls

    /** The Day / Week / Month pills. Keeps the date you were looking at. */
    fun setPeriod(newPeriod: Period) {
        window.value = window.value.copy(period = newPeriod)
    }

    /** The "<" arrow. */
    fun previous() = step(-1)

    /** The ">" arrow. */
    fun next() = step(1)

    private fun step(steps: Long) {
        val current = window.value
        window.value = current.copy(anchor = DateRanges.shift(current.anchor, current.period, steps))
    }
}
