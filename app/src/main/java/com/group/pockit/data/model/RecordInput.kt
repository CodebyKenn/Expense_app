package com.group.pockit.data.model

/** What the "Enter record" screen sends when the user taps SAVE. */
data class RecordInput(
    /** 0 = create a new record, otherwise edit the record with this id. */
    val id: Long = 0,
    val type: RecordType,
    val amount: Double,
    val accountId: Long?,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val timestamp: Long
) {
    /** Returns the first problem found, or null if the record is fine to save. */
    fun validate(): RecordError? = when {
        !amount.isFinite() || amount <= 0.0 -> RecordError.AMOUNT_REQUIRED
        accountId == null -> RecordError.ACCOUNT_REQUIRED
        type == RecordType.TRANSFER && toAccountId == null -> RecordError.TO_ACCOUNT_REQUIRED
        type == RecordType.TRANSFER && toAccountId == accountId -> RecordError.SAME_ACCOUNT
        type != RecordType.TRANSFER && categoryId == null -> RecordError.CATEGORY_REQUIRED
        else -> null
    }
}

/** Every reason a record can be refused. message is ready to show under the field. */
enum class RecordError(val message: String) {
    AMOUNT_REQUIRED("Enter an amount greater than 0"),
    ACCOUNT_REQUIRED("Choose an account"),
    TO_ACCOUNT_REQUIRED("Choose the account to transfer to"),
    SAME_ACCOUNT("Choose two different accounts"),
    CATEGORY_REQUIRED("Choose a category to save")
}

sealed class SaveResult {
    object Success : SaveResult()
    data class Invalid(val error: RecordError) : SaveResult()
}
