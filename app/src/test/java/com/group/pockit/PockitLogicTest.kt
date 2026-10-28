package com.group.pockit

import com.group.pockit.data.model.BudgetOverview
import com.group.pockit.data.model.BudgetStatus
import com.group.pockit.data.model.RecordError
import com.group.pockit.data.model.RecordInput
import com.group.pockit.data.model.RecordType
import com.group.pockit.util.DateRanges
import com.group.pockit.util.Money
import com.group.pockit.util.Period
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Plain unit tests (no phone needed): right-click this file > Run.
 * QA: this covers the "remaining budget" and "over-budget warning" calculations from the requirements.
 */
class PockitLogicTest {

    private fun input(
        type: RecordType = RecordType.EXPENSE,
        amount: Double = 2500.0,
        accountId: Long? = 1,
        toAccountId: Long? = null,
        categoryId: Long? = 3
    ) = RecordInput(
        type = type, amount = amount, accountId = accountId,
        toAccountId = toAccountId, categoryId = categoryId, timestamp = 0L
    )

    // ------------------------------------------------------------ validation

    @Test fun validRecordHasNoError() = assertNull(input().validate())

    @Test fun zeroAmountIsRejected() =
        assertEquals(RecordError.AMOUNT_REQUIRED, input(amount = 0.0).validate())

    @Test fun negativeAmountIsRejected() =
        assertEquals(RecordError.AMOUNT_REQUIRED, input(amount = -50.0).validate())

    @Test fun missingCategoryIsRejectedWithTheDesignMessage() {
        val error = input(categoryId = null).validate()
        assertEquals(RecordError.CATEGORY_REQUIRED, error)
        assertEquals("Choose a category to save", error?.message)
    }

    @Test fun missingAccountIsRejected() =
        assertEquals(RecordError.ACCOUNT_REQUIRED, input(accountId = null).validate())

    @Test fun transferNeedsNoCategoryButNeedsTwoDifferentAccounts() {
        assertNull(input(type = RecordType.TRANSFER, categoryId = null, toAccountId = 2).validate())
        assertEquals(
            RecordError.TO_ACCOUNT_REQUIRED,
            input(type = RecordType.TRANSFER, categoryId = null, toAccountId = null).validate()
        )
        assertEquals(
            RecordError.SAME_ACCOUNT,
            input(type = RecordType.TRANSFER, categoryId = null, toAccountId = 1).validate()
        )
    }

    // ---------------------------------------------------------------- budget

    // Same numbers as the Budget screen in the design
    private val overview = BudgetOverview(
        listOf(
            BudgetStatus(1, "Airtime", "airtime", 500.0, 200.0),
            BudgetStatus(2, "Shopping", "shopping", 1200.0, 1000.0),
            BudgetStatus(3, "Transport", "transport", 600.0, 450.0),
            BudgetStatus(4, "Food", "food", 1500.0, 1650.0),
            BudgetStatus(5, "Entertainment", "entertainment", null, 400.0)
        )
    )

    @Test fun totalsIgnoreCategoriesWithoutALimit() {
        assertEquals(3800.0, overview.totalLimit, 0.001)
        assertEquals(3300.0, overview.totalSpent, 0.001) // Entertainment's 400 is not counted
        assertEquals(500.0, overview.remaining, 0.001)
        assertEquals(87, overview.usedPercent)
        assertFalse(overview.isOver)
    }

    @Test fun overBudgetCategoryShowsHowMuchOver() {
        val food = overview.categories.first { it.name == "Food" }
        assertTrue(food.isOver)
        assertEquals(-150.0, food.remaining, 0.001) // "Ksh 150 over"
    }

    @Test fun nearLimitStartsAtEightyPercent() {
        val shopping = overview.categories.first { it.name == "Shopping" } // 1000 of 1200 = 83%
        assertTrue(shopping.isNearLimit)
        val airtime = overview.categories.first { it.name == "Airtime" } // 200 of 500 = 40%
        assertFalse(airtime.isNearLimit)
    }

    @Test fun categoryWithoutLimitIsNeverOverOrNear() {
        val ent = overview.categories.first { it.name == "Entertainment" }
        assertFalse(ent.hasLimit)
        assertFalse(ent.isOver)
        assertFalse(ent.isNearLimit)
    }

    @Test fun totalBudgetIfSetReplacesTheOldLimit() {
        // Entertainment has no limit: 3800 + 2500 = 6300
        assertEquals(6300.0, overview.totalLimitIfSet(5, 2500.0), 0.001)
        // Food already has 1500: 3800 - 1500 + 2000 = 4300
        assertEquals(4300.0, overview.totalLimitIfSet(4, 2000.0), 0.001)
    }

    @Test fun emptyBudgetHasZeroPercentNotACrash() {
        assertEquals(0, BudgetOverview(emptyList()).usedPercent)
    }

    // ----------------------------------------------------------------- dates

    @Test fun weekRunsMondayToSunday() {
        // Wed 28 Oct 2026 -> Mon 26 Oct to Sun 1 Nov (to is exclusive)
        val range = DateRanges.of(Period.WEEK, LocalDate.of(2026, 10, 28), ZoneOffset.UTC)
        assertEquals("Oct 26 - Nov 1", range.label)
        assertEquals(7L * 24 * 3600 * 1000, range.to - range.from)
    }

    @Test fun monthRangeCoversTheWholeMonth() {
        val range = DateRanges.of(Period.MONTH, LocalDate.of(2026, 10, 28), ZoneOffset.UTC)
        assertEquals("Oct 2026", range.label)
        assertEquals(31L * 24 * 3600 * 1000, range.to - range.from)
    }

    @Test fun arrowsMoveByWholePeriods() {
        val d = LocalDate.of(2026, 10, 28)
        assertEquals(LocalDate.of(2026, 10, 21), DateRanges.shift(d, Period.WEEK, -1))
        assertEquals(LocalDate.of(2026, 9, 28), DateRanges.shift(d, Period.MONTH, -1))
        assertEquals(LocalDate.of(2026, 10, 29), DateRanges.shift(d, Period.DAY, 1))
    }

    // ----------------------------------------------------------------- money

    @Test fun moneyFormatMatchesTheDesign() {
        assertEquals("Ksh 10,500", Money.ksh(10500.0))
        assertEquals("Ksh 812,500.72", Money.ksh(812500.72))
        assertEquals("+ Ksh 5,500", Money.signed(RecordType.INCOME, 5500.0))
        assertEquals("- Ksh 4,800", Money.signed(RecordType.EXPENSE, 4800.0))
    }
}
