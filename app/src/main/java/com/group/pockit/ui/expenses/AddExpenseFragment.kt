package com.group.pockit.ui.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group.pockit.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseFragment : Fragment(R.layout.fragment_add_expense) {

    private var selectedDate = Calendar.getInstance()
    private var transactionType = "Expense"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amountInput = view.findViewById<EditText>(R.id.inputAmount)
        val categorySpinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val accountSpinner = view.findViewById<Spinner>(R.id.spinnerAccount)
        val noteInput = view.findViewById<EditText>(R.id.inputNote)
        val dateButton = view.findViewById<Button>(R.id.buttonDate)
        val saveButton = view.findViewById<Button>(R.id.buttonSaveExpense)

        val incomeButton = view.findViewById<Button>(R.id.buttonIncome)
        val expenseButton = view.findViewById<Button>(R.id.buttonExpense)
        val transferButton = view.findViewById<Button>(R.id.buttonTransfer)
        val cancelButton = view.findViewById<Button>(R.id.buttonCancel)

        updateDateButton(dateButton)
        updateTransactionType(
            "Expense",
            incomeButton,
            expenseButton,
            transferButton,
            categorySpinner
        )

        incomeButton.setOnClickListener {
            updateTransactionType(
                "Income", incomeButton, expenseButton, transferButton, categorySpinner
            )
        }

        expenseButton.setOnClickListener {
            updateTransactionType(
                "Expense", incomeButton, expenseButton, transferButton, categorySpinner
            )
        }

        transferButton.setOnClickListener {
            updateTransactionType(
                "Transfer", incomeButton, expenseButton, transferButton, categorySpinner
            )
        }

        dateButton.setOnClickListener {
            val calendar = selectedDate.clone() as Calendar

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    updateDateButton(dateButton)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        cancelButton.setOnClickListener {
            amountInput.text.clear()
            noteInput.text.clear()
            selectedDate = Calendar.getInstance()
            updateDateButton(dateButton)
            Toast.makeText(requireContext(), "Form cleared", Toast.LENGTH_SHORT).show()
        }

        saveButton.setOnClickListener {
            val amountText = amountInput.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            if (amount == null || !amount.isFinite() || amount <= 0.0) {
                amountInput.error = getString(R.string.amount_error)
                return@setOnClickListener
            }

            val category = categorySpinner.selectedItem?.toString().orEmpty()
            val account = accountSpinner.selectedItem?.toString().orEmpty()
            val note = noteInput.text.toString().trim()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedDate.time)

            if (category.isBlank() || account.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.category_error),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val message = "${getString(R.string.transaction_validated)}: " +
                    "$transactionType, $account, $category, KSh $amount, $date" +
                    if (note.isNotBlank()) ", $note" else ""

            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

            // Permanent saving will be added when the database/repository is connected.
        }
    }

    private fun updateTransactionType(
        type: String,
        incomeButton: Button,
        expenseButton: Button,
        transferButton: Button,
        categorySpinner: Spinner
    ) {
        transactionType = type

        val selectedColor = 0xFF4E285E.toInt()
        val unselectedColor = 0xFFE4D1F1.toInt()
        val selectedTextColor = 0xFFFFFFFF.toInt()
        val unselectedTextColor = 0xFF4E285E.toInt()

        val buttons = listOf(
            incomeButton to "Income",
            expenseButton to "Expense",
            transferButton to "Transfer"
        )

        buttons.forEach { (button, buttonType) ->
            val selected = buttonType == type
            button.setBackgroundColor(if (selected) selectedColor else unselectedColor)
            button.setTextColor(if (selected) selectedTextColor else unselectedTextColor)
        }

        val categories = when (type) {
            "Income" -> R.array.income_categories
            "Transfer" -> R.array.transfer_categories
            else -> R.array.expense_categories
        }

        categorySpinner.setAdapter(
            android.widget.ArrayAdapter.createFromResource(
                requireContext(),
                categories,
                android.R.layout.simple_spinner_item
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        )
    }

    private fun updateDateButton(button: Button) {
        button.text = SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        ).format(selectedDate.time)
    }
}