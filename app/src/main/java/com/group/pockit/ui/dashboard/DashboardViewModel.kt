package com.group.pockit.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.group.pockit.data.PockitRepository
import com.group.pockit.data.Session
import com.group.pockit.data.model.AccountBalance
import com.group.pockit.data.model.RecordItem
import com.group.pockit.data.model.RecordType
import com.group.pockit.util.DateRanges
import com.group.pockit.util.Period
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Dashboard / Home screen.
 *
 * In the Fragment:
 *     private val vm: DashboardViewModel by viewModels()
 *     vm.totalBalance.observe(viewLifecycleOwner) { binding.balance.text = Money.ksh(it) }
 */
class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PockitRepository.get(app)
    private val thisMonth = DateRanges.of(Period.MONTH, LocalDate.now())

    /** "Good Morning, KAREN" */
    val greetingName: String get() = Session.displayName

    /** The big "Total Balance" card. */
    val totalBalance: LiveData<Double> = repo.observeTotalBalance().asLiveData()

    /** The M-Pesa / Cash / Bank chips inside the card. */
    val accounts: LiveData<List<AccountBalance>> = repo.observeBalances().asLiveData()

    /** The green "Income" and red "Expense" boxes. These are for the current month. */
    val monthIncome: LiveData<Double> = repo.observeTotal(RecordType.INCOME, thisMonth).asLiveData()
    val monthExpense: LiveData<Double> = repo.observeTotal(RecordType.EXPENSE, thisMonth).asLiveData()

    /** "Recent transaction" list. Each item: categoryName, accountName, timestamp, amount, type. */
    val recent: LiveData<List<RecordItem>> = repo.observeRecent(10).asLiveData()

    // ----- the magnifier icon

    private val searchText = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: LiveData<List<RecordItem>> = searchText
        .flatMapLatest { text -> if (text.isBlank()) flowOf(emptyList<RecordItem>()) else repo.search(text) }
        .asLiveData()

    /** Call on every keystroke. An empty text clears the results. */
    fun search(text: String) {
        searchText.value = text
    }

    init {
        // first launch for a new user: create M-Pesa, Cash and Bank
        viewModelScope.launch { repo.ensureDefaultAccounts() }
    }

    /** Debug helper: fills the app with sample data for the demo. Does nothing if records already exist. */
    fun loadDemoData() {
        viewModelScope.launch { repo.seedDemoData() }
    }
}
