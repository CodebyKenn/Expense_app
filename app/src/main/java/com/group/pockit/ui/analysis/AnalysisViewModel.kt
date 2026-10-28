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

class AnalysisViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PockitRepository.get(app)

    private data class Window(val period: Period, val anchor: LocalDate)

    private val window = MutableStateFlow(Window(Period.MONTH, LocalDate.now()))
    private val range = window.map { DateRanges.of(it.period, it.anchor) }

    private val recordTypeFlow = MutableStateFlow(RecordType.EXPENSE)
    val recordType: LiveData<RecordType> = recordTypeFlow.asLiveData()

    /** Which of Day / Week / Month is selected. */
    val period: LiveData<Period> = window.map { it.period }.asLiveData()

    /** "Oct 19 - Oct 25" */
    val rangeLabel: LiveData<String> = range.map { it.label }.asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeTotal: LiveData<Double> = combine(range, recordTypeFlow) { r, t -> r to t }
        .flatMapLatest { (r, t) -> repo.observeTotal(t, r) }
        .asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeBreakdown: LiveData<List<CategoryShare>> = combine(range, recordTypeFlow) { r, t -> r to t }
        .flatMapLatest { (r, t) -> repo.observeBreakdown(t, r) }
        .asLiveData()

    @OptIn(ExperimentalCoroutinesApi::class)
    val accountFlows: LiveData<List<AccountFlow>> =
        range.flatMapLatest { repo.observeAccountFlows(it) }.asLiveData()

    // ----- controls

    fun setRecordType(type: RecordType) {
        recordTypeFlow.value = type
    }

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
