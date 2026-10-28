package com.group.pockit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.group.pockit.data.dao.AccountDao
import com.group.pockit.data.dao.BudgetDao
import com.group.pockit.data.dao.CategoryDao
import com.group.pockit.data.dao.RecordDao
import com.group.pockit.data.model.Account
import com.group.pockit.data.model.Budget
import com.group.pockit.data.model.Category
import com.group.pockit.data.model.MoneyRecord

/**
 * The SQLite database of the app (Room builds it from the entities listed below).
 * The file lives on the phone, so everything works offline and survives a restart.
 *
 * If you ever change an entity (add or rename a column), raise `version` AND uninstall the
 * app from the emulator/phone once, otherwise it will crash on start with an "identity hash" error.
 */
@Database(
    entities = [Account::class, Category::class, MoneyRecord::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recordDao(): RecordDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** One shared database for the whole app. Always get it through here. */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pockit.db"
                )
                    .addCallback(SeedCategories)
                    .build()
                    .also { instance = it }
            }
    }

    /** Runs once, the very first time the database file is created: fills in the category lists. */
    private object SeedCategories : RoomDatabase.Callback() {

        // name to iconKey. The frontend maps each iconKey to a drawable.
        private val expense = listOf(
            "Shopping" to "shopping",
            "Food" to "food",
            "Health" to "health",
            "Airtime" to "airtime",
            "Transport" to "transport",
            "Entertainment" to "entertainment",
            "Bills" to "bills",
            "Education" to "education",
            "Other" to "other"
        )

        private val income = listOf(
            "Allowance" to "allowance",
            "Work" to "work",
            "Gifts" to "gift",
            "Investments" to "investments",
            "Other" to "other"
        )

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            expense.forEach { (name, icon) ->
                db.execSQL("INSERT INTO categories (name, type, iconKey) VALUES ('$name', 'EXPENSE', '$icon')")
            }
            income.forEach { (name, icon) ->
                db.execSQL("INSERT INTO categories (name, type, iconKey) VALUES ('$name', 'INCOME', '$icon')")
            }
        }
    }
}
