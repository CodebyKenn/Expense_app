package com.group.pockit.ui.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.group.pockit.data.PockitRepository
import com.group.pockit.data.model.AccountBalance
import kotlinx.coroutines.launch

/** "All Accounts" screen: the total card, the list, "ADD NEW ACCOUNT" and the "..." menu. */
class AccountsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PockitRepository.get(app)

    /** The purple "Total Balance" card. */
    val totalBalance: LiveData<Double> = repo.observeTotalBalance().asLiveData()

    /** The list: name, iconKey, balance for each account. */
    val accounts: LiveData<List<AccountBalance>> = repo.observeBalances().asLiveData()

    init {
        viewModelScope.launch { repo.ensureDefaultAccounts() }
    }

    /** "ADD NEW ACCOUNT". onResult(false) means the name was blank and nothing was saved. */
    fun addAccount(name: String, openingBalance: Double = 0.0, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.addAccount(name, openingBalance)) }
    }

    /** "..." menu > Rename. */
    fun rename(accountId: Long, newName: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.renameAccount(accountId, newName)) }
    }

    /** "..." menu > Edit Name & Balance. */
    fun updateAccount(accountId: Long, newName: String, openingBalance: Double, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.updateAccount(accountId, newName, openingBalance)) }
    }

    /** "..." menu > Delete. Warning: also deletes that account's records. Ask the user to confirm first. */
    fun delete(accountId: Long) {
        viewModelScope.launch { repo.deleteAccount(accountId) }
    }

    /** Clears all user transactions, budgets, custom accounts and resets default accounts. */
    fun resetData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repo.clearUserData()
            onComplete()
        }
    }

    /** Clears existing data and loads sample dummy data. */
    fun loadDemoData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repo.forceSeedDemoData()
            onComplete()
        }
    }
}
