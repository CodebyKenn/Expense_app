package com.group.pockit.data

import android.content.Context
import com.group.pockit.data.model.Account
import com.group.pockit.data.model.AccountBalance
import com.group.pockit.data.model.AccountFlow
import com.group.pockit.data.model.Budget
import com.group.pockit.data.model.BudgetOverview
import com.group.pockit.data.model.Category
import com.group.pockit.data.model.CategoryShare
import com.group.pockit.data.model.MoneyRecord
import com.group.pockit.data.model.RecordInput
import com.group.pockit.data.model.RecordItem
import com.group.pockit.data.model.RecordType
import com.group.pockit.data.model.SaveResult
import com.group.pockit.util.DateRange
import com.group.pockit.util.DateRanges
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * The only class the ViewModels talk to. It hides the four DAOs, applies the rules
 * (validation, percentages, who the current user is) and returns Flows that keep updating:
 * whenever the data changes, every screen showing it refreshes by itself.
 *
 * Functions starting with "observe" return a Flow (live data). The others are one-off
 * actions and must be called from a coroutine (viewModelScope.launch { ... }).
 */
class PockitRepository private constructor(db: AppDatabase) {

    private val accountDao = db.accountDao()
    private val categoryDao = db.categoryDao()
    private val recordDao = db.recordDao()
    private val budgetDao = db.budgetDao()

    private val setupLock = Mutex()

    private val userId: String get() = Session.userId

    // ------------------------------------------------------------ first run

    /** Creates M-Pesa, Cash and Bank for a brand-new user. Safe to call as often as you like. */
    suspend fun ensureDefaultAccounts() {
        setupLock.withLock {
            if (accountDao.count(userId) == 0) {
                accountDao.insert(Account(userId = userId, name = "M-Pesa", iconKey = "mpesa"))
                accountDao.insert(Account(userId = userId, name = "Cash", iconKey = "cash"))
                accountDao.insert(Account(userId = userId, name = "Bank", iconKey = "bank"))
            }
        }
    }

    // ------------------------------------------------------------- accounts

    fun observeBalances(): Flow<List<AccountBalance>> = accountDao.observeBalances(userId)

    /** The big "Total Balance" card: every account added together. */
    fun observeTotalBalance(): Flow<Double> =
        observeBalances().map { list -> list.sumOf { it.balance } }

    /** Returns false (and saves nothing) when the name is blank. */
    suspend fun addAccount(name: String, openingBalance: Double = 0.0, iconKey: String = "wallet"): Boolean {
        val clean = name.trim()
        if (clean.isEmpty()) return false
        accountDao.insert(Account(userId = userId, name = clean, iconKey = iconKey, openingBalance = openingBalance))
        return true
    }

    suspend fun renameAccount(id: Long, newName: String): Boolean {
        val clean = newName.trim()
        if (clean.isEmpty()) return false
        return accountDao.rename(id, userId, clean) > 0
    }

    suspend fun updateAccount(id: Long, newName: String, openingBalance: Double): Boolean {
        val clean = newName.trim()
        if (clean.isEmpty() || !openingBalance.isFinite()) return false
        return accountDao.updateAccount(id, userId, clean, openingBalance) > 0
    }

    /** Also deletes every record of that account (and transfers into it). */
    suspend fun deleteAccount(id: Long) {
        accountDao.delete(id, userId)
    }

    suspend fun firstAccountId(): Long? = accountDao.firstId(userId)

    // ----------------------------------------------------------- categories

    /** For the category picker. type = INCOME or EXPENSE. */
    fun observeCategories(type: RecordType): Flow<List<Category>> = categoryDao.observeByType(type)

    // -------------------------------------------------------------- records

    /** Validates, then inserts (id = 0) or updates (id > 0). The screen shows result.error if invalid. */
    suspend fun saveRecord(input: RecordInput): SaveResult {
        val problem = input.validate()
        if (problem != null) return SaveResult.Invalid(problem)

        val isTransfer = input.type == RecordType.TRANSFER
        val record = MoneyRecord(
            id = input.id,
            userId = userId,
            type = input.type,
            amount = input.amount,
            accountId = input.accountId!!, // validate() guarantees it is set
            toAccountId = if (isTransfer) input.toAccountId else null,
            categoryId = if (isTransfer) null else input.categoryId,
            note = input.note.trim(),
            timestamp = input.timestamp
        )
        if (input.id == 0L) recordDao.insert(record) else recordDao.update(record)
        return SaveResult.Success
    }

    suspend fun getRecord(id: Long): MoneyRecord? = recordDao.getById(id, userId)

    suspend fun deleteRecord(record: MoneyRecord) = recordDao.delete(record)

    fun observeRecent(limit: Int = 10): Flow<List<RecordItem>> = recordDao.observeRecent(userId, limit)

    fun observeRecords(range: DateRange): Flow<List<RecordItem>> =
        recordDao.observeInRange(userId, range.from, range.to)

    fun search(text: String): Flow<List<RecordItem>> = recordDao.search(userId, text.trim())

    // ------------------------------------------------------------- analysis

    fun observeTotal(type: RecordType, range: DateRange): Flow<Double> =
        recordDao.observeTotal(userId, type, range.from, range.to)

    /** Donut chart data with each category's share in percent (0 to 100). */
    fun observeBreakdown(type: RecordType, range: DateRange): Flow<List<CategoryShare>> =
        if (type == RecordType.TRANSFER) observeTransferBreakdown(range)
        else recordDao.observeTotalsByCategory(userId, type, range.from, range.to).map { rows ->
            val sum = rows.sumOf { it.total }
            rows.map {
                val percent = if (sum > 0.0) Math.round(it.total / sum * 100).toInt() else 0
                CategoryShare(it.categoryId, it.name, it.iconKey, it.total, percent)
            }
        }

    fun observeTransferBreakdown(range: DateRange): Flow<List<CategoryShare>> =
        recordDao.observeTransferTotals(userId, range.from, range.to).map { rows ->
            val sum = rows.sumOf { it.total }
            rows.map {
                val percent = if (sum > 0.0) Math.round(it.total / sum * 100).toInt() else 0
                CategoryShare(it.categoryId, it.name, it.iconKey, it.total, percent)
            }
        }

    fun observeAccountFlows(range: DateRange): Flow<List<AccountFlow>> =
        recordDao.observeAccountFlows(userId, range.from, range.to)

    // -------------------------------------------------------------- budgets

    fun observeBudget(month: YearMonth): Flow<BudgetOverview> {
        val range = DateRanges.monthRange(month)
        return budgetDao.observeStatus(userId, month.toString(), range.from, range.to)
            .map { BudgetOverview(it) }
    }

    /** Returns false (and saves nothing) when the limit is 0 or negative. */
    suspend fun setBudget(month: YearMonth, categoryId: Long, limit: Double): Boolean {
        if (!limit.isFinite() || limit <= 0.0) return false
        budgetDao.upsert(
            Budget(userId = userId, month = month.toString(), categoryId = categoryId, limitAmount = limit)
        )
        return true
    }

    suspend fun removeBudget(month: YearMonth, categoryId: Long) {
        budgetDao.remove(userId, month.toString(), categoryId)
    }

    suspend fun addBudgetCategory(name: String, limitAmount: Double): Boolean {
        val clean = name.trim()
        if (clean.isEmpty() || !limitAmount.isFinite() || limitAmount <= 0.0) return false
        val catId = categoryDao.insert(Category(name = clean, type = RecordType.EXPENSE, iconKey = "other"))
        return setBudget(YearMonth.now(), catId, limitAmount)
    }

    // ------------------------------------------------------------ demo data

    /** Deletes all records, budgets, and custom accounts for the current user, then recreates default accounts with 0 balance. */
    suspend fun clearUserData() {
        recordDao.deleteAll(userId)
        budgetDao.deleteAll(userId)
        accountDao.deleteAll(userId)
        ensureDefaultAccounts()
    }

    /** Clears all current data and populates sample transactions and budgets. */
    suspend fun forceSeedDemoData() {
        clearUserData()
        seedDemoData()
    }

    /**
     * Fills the app with sample data that matches the design screens, so the Dashboard,
     * Analysis and Budget screens look alive without typing 15 records by hand.
     * Does nothing if the user already has records. Dates are relative to today.
     */
    suspend fun seedDemoData() {
        ensureDefaultAccounts()
        if (recordDao.count(userId) > 0) return

        val accounts = accountDao.getAll(userId).associateBy { it.name }
        val mpesa = accounts["M-Pesa"]?.id ?: return
        val cash = accounts["Cash"]?.id ?: return
        val bank = accounts["Bank"]?.id ?: return
        val expenseCats = categoryDao.getByType(RecordType.EXPENSE).associateBy { it.name }
        val incomeCats = categoryDao.getByType(RecordType.INCOME).associateBy { it.name }

        val today = LocalDate.now()
        // never go back past the 1st of this month, so the demo always fits in the current month
        fun at(daysAgo: Long, hour: Int): Long {
            val day = today.minusDays(minOf(daysAgo, (today.dayOfMonth - 1).toLong()))
            return DateRanges.toMillis(day, LocalTime.of(hour, 0))
        }

        fun incomeRecord(account: Long, category: String, amount: Double, daysAgo: Long, note: String) =
            MoneyRecord(
                userId = userId, type = RecordType.INCOME, amount = amount, accountId = account,
                categoryId = incomeCats[category]?.id, note = note, timestamp = at(daysAgo, 8)
            )

        fun expenseRecord(account: Long, category: String, amount: Double, daysAgo: Long, hour: Int, note: String) =
            MoneyRecord(
                userId = userId, type = RecordType.EXPENSE, amount = amount, accountId = account,
                categoryId = expenseCats[category]?.id, note = note, timestamp = at(daysAgo, hour)
            )

        listOf(
            incomeRecord(mpesa, "Allowance", 12_000.0, 6, "Monthly allowance"),
            incomeRecord(bank, "Work", 8_000.0, 5, "Weekend gig"),
            incomeRecord(cash, "Gifts", 3_000.0, 4, "Birthday money"),
            expenseRecord(mpesa, "Shopping", 1_000.0, 0, 9, "Groceries"),
            expenseRecord(cash, "Food", 1_650.0, 1, 13, "Lunch and snacks"),
            expenseRecord(mpesa, "Transport", 450.0, 2, 7, "Matatu fare"),
            expenseRecord(mpesa, "Airtime", 200.0, 3, 18, "Safaricom airtime"),
            expenseRecord(cash, "Entertainment", 400.0, 3, 20, "Movie"),
            MoneyRecord(
                userId = userId, type = RecordType.TRANSFER, amount = 2_000.0,
                accountId = bank, toAccountId = mpesa, note = "Top up M-Pesa", timestamp = at(1, 10)
            )
        ).forEach { recordDao.insert(it) }

        val month = YearMonth.now()
        mapOf("Airtime" to 500.0, "Shopping" to 1_200.0, "Transport" to 600.0, "Food" to 1_500.0)
            .forEach { (name, limit) ->
                expenseCats[name]?.let { setBudget(month, it.id, limit) }
            }
    }

    companion object {
        @Volatile
        private var instance: PockitRepository? = null

        /** One shared repository for the whole app. */
        fun get(context: Context): PockitRepository =
            instance ?: synchronized(this) {
                instance ?: PockitRepository(AppDatabase.getInstance(context)).also { instance = it }
            }
    }
}
