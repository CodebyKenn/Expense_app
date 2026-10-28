package com.group.pockit.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.group.pockit.data.model.Category
import com.group.pockit.data.model.RecordType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category): Long

    /** Categories for the picker. Pass INCOME or EXPENSE. Order = the order they were created in. */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id")
    fun observeByType(type: RecordType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id")
    suspend fun getByType(type: RecordType): List<Category>
}
