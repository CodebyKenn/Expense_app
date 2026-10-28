package com.group.pockit.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.group.pockit.data.model.AccountFlow
import com.group.pockit.data.model.CategoryTotal
import com.group.pockit.data.model.MoneyRecord
import com.group.pockit.data.model.RecordItem
import com.group.pockit.data.model.RecordType
import kotlinx.coroutines.flow.Flow

/**
 * Time windows are [fromTime, toTime): fromTime included, toTime excluded, both epoch millis.
 * DateRanges.of(...) builds them for Day / Week / Month.
 */
@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: MoneyRecord): Long

    @Update
    suspend fun update(record: MoneyRecord)

    @Delete
    suspend fun delete(record: MoneyRecord)

    @Query("SELECT * FROM records WHERE id = :id AND userId = :userId")
    suspend fun getById(id: Long, userId: String): MoneyRecord?

    @Query("SELECT COUNT(*) FROM records WHERE userId = :userId")
    suspend fun count(userId: String): Int

    // ---------------------------------------------------------------- lists

    /** Dashboard "Recent transaction" list: newest first. */
    @Query(
        """
        SELECT r.id AS id, r.type AS type, r.amount AS amount, r.note AS note, r.timestamp AS timestamp,
               r.accountId AS accountId, a.name AS accountName, ta.name AS toAccountName,
               r.categoryId AS categoryId, c.name AS categoryName, c.iconKey AS categoryIconKey
        FROM records r
        JOIN accounts a ON a.id = r.accountId
        LEFT JOIN accounts ta ON ta.id = r.toAccountId
        LEFT JOIN categories c ON c.id = r.categoryId
        WHERE r.userId = :userId
        ORDER BY r.timestamp DESC, r.id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(userId: String, limit: Int): Flow<List<RecordItem>>

    /** All records inside a window, newest first (a full history list for a Day / Week / Month). */
    @Query(
        """
        SELECT r.id AS id, r.type AS type, r.amount AS amount, r.note AS note, r.timestamp AS timestamp,
               r.accountId AS accountId, a.name AS accountName, ta.name AS toAccountName,
               r.categoryId AS categoryId, c.name AS categoryName, c.iconKey AS categoryIconKey
        FROM records r
        JOIN accounts a ON a.id = r.accountId
        LEFT JOIN accounts ta ON ta.id = r.toAccountId
        LEFT JOIN categories c ON c.id = r.categoryId
        WHERE r.userId = :userId AND r.timestamp >= :fromTime AND r.timestamp < :toTime
        ORDER BY r.timestamp DESC, r.id DESC
        """
    )
    fun observeInRange(userId: String, fromTime: Long, toTime: Long): Flow<List<RecordItem>>

    /** The magnifier icon on the Dashboard: matches category name, note or account name. */
    @Query(
        """
        SELECT r.id AS id, r.type AS type, r.amount AS amount, r.note AS note, r.timestamp AS timestamp,
               r.accountId AS accountId, a.name AS accountName, ta.name AS toAccountName,
               r.categoryId AS categoryId, c.name AS categoryName, c.iconKey AS categoryIconKey
        FROM records r
        JOIN accounts a ON a.id = r.accountId
        LEFT JOIN accounts ta ON ta.id = r.toAccountId
        LEFT JOIN categories c ON c.id = r.categoryId
        WHERE r.userId = :userId
          AND (c.name LIKE '%' || :text || '%'
               OR r.note LIKE '%' || :text || '%'
               OR a.name LIKE '%' || :text || '%')
        ORDER BY r.timestamp DESC, r.id DESC
        LIMIT 100
        """
    )
    fun search(userId: String, text: String): Flow<List<RecordItem>>

    // ------------------------------------------------------------- analysis

    /** Total of one type (INCOME or EXPENSE) in a window. 0 when there are no records. */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM records
        WHERE userId = :userId AND type = :type AND timestamp >= :fromTime AND timestamp < :toTime
        """
    )
    fun observeTotal(userId: String, type: RecordType, fromTime: Long, toTime: Long): Flow<Double>

    /** Donut chart + rows: one line per category, biggest first. */
    @Query(
        """
        SELECT c.id AS categoryId, c.name AS name, c.iconKey AS iconKey, SUM(r.amount) AS total
        FROM records r
        JOIN categories c ON c.id = r.categoryId
        WHERE r.userId = :userId AND r.type = :type AND r.timestamp >= :fromTime AND r.timestamp < :toTime
        GROUP BY c.id
        ORDER BY total DESC
        """
    )
    fun observeTotalsByCategory(userId: String, type: RecordType, fromTime: Long, toTime: Long): Flow<List<CategoryTotal>>

    /**
     * "Accounts" analysis tab: income and expense per account in a window.
     * Accounts with no activity still appear, with 0 and 0.
     */
    @Query(
        """
        SELECT a.id AS accountId, a.name AS name,
               COALESCE(SUM(CASE WHEN r.type = 'INCOME' THEN r.amount END), 0) AS income,
               COALESCE(SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount END), 0) AS expense
        FROM accounts a
        LEFT JOIN records r ON r.accountId = a.id AND r.timestamp >= :fromTime AND r.timestamp < :toTime
        WHERE a.userId = :userId
        GROUP BY a.id
        ORDER BY a.id
        """
    )
    fun observeAccountFlows(userId: String, fromTime: Long, toTime: Long): Flow<List<AccountFlow>>
}
