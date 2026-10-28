package com.group.pockit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.group.pockit.data.model.Budget
import com.group.pockit.data.model.BudgetStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    /** Insert, or replace the existing limit for the same user + month + category. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget): Long

    @Query("DELETE FROM budgets WHERE userId = :userId AND month = :month AND categoryId = :categoryId")
    suspend fun remove(userId: String, month: String, categoryId: Long): Int

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAll(userId: String): Int

    /**
     * The whole Budget screen in one query: EVERY expense category, with its limit for the
     * month (null if none set) and how much has been spent in it during that month.
     * month is "yyyy-MM", fromTime / toTime are the start of that month and the start of the next.
     */
    @Query(
        """
        SELECT c.id AS categoryId, c.name AS name, c.iconKey AS iconKey,
               b.limitAmount AS limitAmount,
               COALESCE((SELECT SUM(r.amount) FROM records r
                         WHERE r.userId = :userId AND r.categoryId = c.id AND r.type = 'EXPENSE'
                           AND r.timestamp >= :fromTime AND r.timestamp < :toTime), 0) AS spent
        FROM categories c
        LEFT JOIN budgets b ON b.categoryId = c.id AND b.userId = :userId AND b.month = :month
        WHERE c.type = 'EXPENSE'
        ORDER BY c.id
        """
    )
    fun observeStatus(userId: String, month: String, fromTime: Long, toTime: Long): Flow<List<BudgetStatus>>
}
