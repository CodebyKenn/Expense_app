package com.group.pockit.data.model

/**
 * What kind of money movement a record is.
 * Categories only ever use INCOME or EXPENSE. TRANSFER moves money between two accounts.
 */
enum class RecordType { INCOME, EXPENSE, TRANSFER }
