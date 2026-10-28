package com.group.pockit.ui.record

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.group.pockit.data.PockitRepository
import com.group.pockit.data.model.AccountBalance
import com.group.pockit.data.model.Category
import com.group.pockit.data.model.RecordError
import com.group.pockit.data.model.RecordInput
import com.group.pockit.data.model.RecordType
import com.group.pockit.data.model.SaveResult
import com.group.pockit.util.DateRanges
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * "Enter record" screen (new record, or editing an existing one).
 *
 * The keypad sets `amount`, the toggles / pickers set the other fields, and SAVE calls save().
 *
 *     vm.error.observe(viewLifecycleOwner) { binding.categoryError.text = it?.message }
 *     vm.saved.observe(viewLifecycleOwner) { if (it) findNavController().popBackStack() }
 */
class RecordViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PockitRepository.get(app)
    private var editingId = 0L

    // ----- the form fields (all LiveData, so the screen can show the current choice)

    private val typeFlow = MutableStateFlow(RecordType.EXPENSE)
    val type: LiveData<RecordType> = typeFlow.asLiveData()

    val accountId = MutableLiveData<Long?>(null)
    val toAccountId = MutableLiveData<Long?>(null)
    val categoryId = MutableLiveData<Long?>(null)
    val amount = MutableLiveData(0.0)
    val date = MutableLiveData(LocalDate.now())
    val time = MutableLiveData(LocalTime.now().withSecond(0).withNano(0))
    var note: String = ""

    // ----- what the pickers list

    /** Account picker. Each item has id, name, iconKey, balance. */
    val accounts: LiveData<List<AccountBalance>> = repo.observeBalances().asLiveData()

    /** Category picker. Changes by itself when the Income / Expense / Transfer tab changes. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: LiveData<List<Category>> = typeFlow
        .flatMapLatest { t -> if (t == RecordType.TRANSFER) flowOf(emptyList<Category>()) else repo.observeCategories(t) }
        .asLiveData()

    // ----- results

    private val _error = MutableLiveData<RecordError?>(null)

    /** Null = no problem. Otherwise show error.message (e.g. "Choose a category to save"). */
    val error: LiveData<RecordError?> = _error

    private val _saved = MutableLiveData(false)

    /** Turns true once the record is stored. Close the screen when you see it. */
    val saved: LiveData<Boolean> = _saved

    init {
        // preselect the first account (M-Pesa), like the design
        viewModelScope.launch {
            repo.ensureDefaultAccounts()
            if (accountId.value == null) accountId.value = repo.firstAccountId()
        }
    }

    /** Income / Expense / Transfer tabs. Clears the category, because the lists differ. */
    fun setType(newType: RecordType) {
        typeFlow.value = newType
        categoryId.value = null
        _error.value = null
    }

    fun save() {
        val input = RecordInput(
            id = editingId,
            type = typeFlow.value,
            amount = amount.value ?: 0.0,
            accountId = accountId.value,
            toAccountId = toAccountId.value,
            categoryId = categoryId.value,
            note = note,
            timestamp = DateRanges.toMillis(
                date.value ?: LocalDate.now(),
                time.value ?: LocalTime.now()
            )
        )
        viewModelScope.launch {
            when (val result = repo.saveRecord(input)) {
                is SaveResult.Invalid -> _error.value = result.error
                SaveResult.Success -> {
                    _error.value = null
                    _saved.value = true
                }
            }
        }
    }

    /** Opens an existing record for editing: fills every field, and save() will update it. */
    fun loadForEdit(recordId: Long) {
        viewModelScope.launch {
            val record = repo.getRecord(recordId) ?: return@launch
            editingId = record.id
            typeFlow.value = record.type
            accountId.value = record.accountId
            toAccountId.value = record.toAccountId
            categoryId.value = record.categoryId
            amount.value = record.amount
            note = record.note
            val moment = Instant.ofEpochMilli(record.timestamp).atZone(ZoneId.systemDefault())
            date.value = moment.toLocalDate()
            time.value = moment.toLocalTime().withSecond(0).withNano(0)
        }
    }

    /** Delete button on an existing record. Closes the screen via `saved`. */
    fun deleteCurrent() {
        if (editingId == 0L) return
        viewModelScope.launch {
            repo.getRecord(editingId)?.let { repo.deleteRecord(it) }
            _saved.value = true
        }
    }
}
