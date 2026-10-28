package com.group.pockit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.group.pockit.data.model.Account
import com.group.pockit.data.model.AccountBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert
    suspend fun insert(account: Account): Long

    @Query("SELECT COUNT(*) FROM accounts WHERE userId = :userId")
    suspend fun count(userId: String): Int

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY id")
    suspend fun getAll(userId: String): List<Account>

    @Query("SELECT id FROM accounts WHERE userId = :userId ORDER BY id LIMIT 1")
    suspend fun firstId(userId: String): Long?

    @Query("UPDATE accounts SET name = :name WHERE id = :id AND userId = :userId")
    suspend fun rename(id: Long, userId: String, name: String): Int

    @Query("DELETE FROM accounts WHERE id = :id AND userId = :userId")
    suspend fun delete(id: Long, userId: String): Int

    /**
     * Every account with its CURRENT balance:
     *   opening balance + income - expenses - transfers out + transfers in.
     * Re-emits automatically whenever a record or account changes.
     */
    @Query(
        """
        SELECT a.id AS id, a.name AS name, a.iconKey AS iconKey,
               a.openingBalance
               + COALESCE((SELECT SUM(r.amount) FROM records r WHERE r.accountId = a.id AND r.type = 'INCOME'), 0)
               - COALESCE((SELECT SUM(r.amount) FROM records r WHERE r.accountId = a.id AND r.type = 'EXPENSE'), 0)
               - COALESCE((SELECT SUM(r.amount) FROM records r WHERE r.accountId = a.id AND r.type = 'TRANSFER'), 0)
               + COALESCE((SELECT SUM(r.amount) FROM records r WHERE r.toAccountId = a.id AND r.type = 'TRANSFER'), 0)
               AS balance
        FROM accounts a
        WHERE a.userId = :userId
        ORDER BY a.id
        """
    )
    fun observeBalances(userId: String): Flow<List<AccountBalance>>
}
