package com.group.pockit

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun remainingBudget_isCalculatedCorrectly() {
        val budget = 1000.0
        val expenses = 300.0
        val remaining = budget - expenses

        assertEquals(700.0, remaining, 0.0)
    }

    @Test
    fun overBudget_isDetected() {
        val budget = 1000.0
        val expenses = 1200.0
        val overBudget = expenses > budget
        val amountOver = expenses - budget

        assertTrue(overBudget)
        assertEquals(200.0, amountOver, 0.0)
    }

    @Test
    fun spendingExactlyBudget_isNotOverBudget() {
        val budget = 1000.0
        val expenses = 1000.0

        assertFalse(expenses > budget)
        assertEquals(0.0, budget - expenses, 0.0)
    }
}