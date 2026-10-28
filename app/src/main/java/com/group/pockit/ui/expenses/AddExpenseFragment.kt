package com.group.pockit.ui.expenses

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.group.pockit.R
import com.group.pockit.data.PockitRepository
import com.group.pockit.data.model.AccountBalance
import com.group.pockit.data.model.Category
import com.group.pockit.data.model.RecordInput
import com.group.pockit.data.model.RecordType
import com.group.pockit.data.model.SaveResult
import com.group.pockit.util.SuccessDialog
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseFragment : Fragment(R.layout.fragment_add_expense) {

    private var selectedDate = Calendar.getInstance()
    private var transactionType = "Expense"
    private var accountList: List<AccountBalance> = emptyList()
    private var currentCategories: List<Category> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amountInput = view.findViewById<EditText>(R.id.inputAmount)
        val categorySpinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val accountSpinner = view.findViewById<Spinner>(R.id.spinnerAccount)
        val toAccountSpinner = view.findViewById<Spinner>(R.id.spinnerToAccount)
        val tvAccountLabel = view.findViewById<TextView>(R.id.tvAccountLabel)
        val layoutCategory = view.findViewById<View>(R.id.layoutCategory)
        val layoutToAccount = view.findViewById<View>(R.id.layoutToAccount)

        val dateButton = view.findViewById<Button>(R.id.buttonDate)
        val timeText = view.findViewById<TextView>(R.id.textTime)
        val saveButtonHeader = view.findViewById<TextView>(R.id.buttonSaveExpense)
        val saveButtonBottom = view.findViewById<Button>(R.id.buttonSaveExpenseBottom)

        val incomeButton = view.findViewById<Button>(R.id.buttonIncome)
        val expenseButton = view.findViewById<Button>(R.id.buttonExpense)
        val transferButton = view.findViewById<Button>(R.id.buttonTransfer)
        val cancelButton = view.findViewById<TextView>(R.id.buttonCancel)

        amountInput.showSoftInputOnFocus = false

        setupKeypad(view, amountInput)

        updateDateButton(dateButton)
        updateTimeText(timeText)

        val repo = PockitRepository.get(requireContext())

        // Populate accounts dynamically from repository
        lifecycleScope.launch {
            repo.ensureDefaultAccounts()
            repo.observeBalances().collect { accounts ->
                accountList = accounts
                val names = if (accounts.isNotEmpty()) accounts.map { it.name }
                else resources.getStringArray(R.array.expense_accounts).toList()

                val accountAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    names
                ).apply {
                    setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                accountSpinner.adapter = accountAdapter

                val toAccountAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    names
                ).apply {
                    setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                toAccountSpinner.adapter = toAccountAdapter
                if (names.size > 1) {
                    toAccountSpinner.setSelection(1)
                }
            }
        }

        updateTransactionType(
            "Expense",
            incomeButton,
            expenseButton,
            transferButton,
            categorySpinner,
            tvAccountLabel,
            layoutCategory,
            layoutToAccount,
            repo
        )

        incomeButton.setOnClickListener {
            updateTransactionType(
                "Income",
                incomeButton,
                expenseButton,
                transferButton,
                categorySpinner,
                tvAccountLabel,
                layoutCategory,
                layoutToAccount,
                repo
            )
        }

        expenseButton.setOnClickListener {
            updateTransactionType(
                "Expense",
                incomeButton,
                expenseButton,
                transferButton,
                categorySpinner,
                tvAccountLabel,
                layoutCategory,
                layoutToAccount,
                repo
            )
        }

        transferButton.setOnClickListener {
            updateTransactionType(
                "Transfer",
                incomeButton,
                expenseButton,
                transferButton,
                categorySpinner,
                tvAccountLabel,
                layoutCategory,
                layoutToAccount,
                repo
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
            selectedDate = Calendar.getInstance()
            updateDateButton(dateButton)
            updateTimeText(timeText)
            Toast.makeText(requireContext(), "Form cleared", Toast.LENGTH_SHORT).show()
        }

        val onSaveClicked = {
            saveTransaction(amountInput, categorySpinner, accountSpinner, toAccountSpinner, repo)
        }

        saveButtonHeader.setOnClickListener { onSaveClicked() }
        saveButtonBottom?.setOnClickListener { onSaveClicked() }
    }

    private fun saveTransaction(
        amountInput: EditText,
        categorySpinner: Spinner,
        accountSpinner: Spinner,
        toAccountSpinner: Spinner,
        repo: PockitRepository
    ) {
        val amountText = amountInput.text.toString().trim()
        val amount = amountText.toDoubleOrNull()

        if (amount == null || !amount.isFinite() || amount <= 0.0) {
            amountInput.error = getString(R.string.amount_error)
            return
        }

        val recType = when (transactionType) {
            "Income" -> RecordType.INCOME
            "Transfer" -> RecordType.TRANSFER
            else -> RecordType.EXPENSE
        }

        lifecycleScope.launch {
            val fromAccountName = accountSpinner.selectedItem?.toString().orEmpty()
            val selectedFromAccount = accountList.find { it.name.equals(fromAccountName, ignoreCase = true) }
            val accountId = selectedFromAccount?.id ?: repo.firstAccountId()

            if (accountId == null) {
                Toast.makeText(requireContext(), "Choose a valid account", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (recType == RecordType.TRANSFER) {
                val toAccountName = toAccountSpinner.selectedItem?.toString().orEmpty()
                val selectedToAccount = accountList.find { it.name.equals(toAccountName, ignoreCase = true) }
                val toAccountId = selectedToAccount?.id

                if (toAccountId == null) {
                    Toast.makeText(requireContext(), "Choose an account to transfer to", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (accountId == toAccountId) {
                    Toast.makeText(requireContext(), "Choose two different accounts for transfer", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val input = RecordInput(
                    type = RecordType.TRANSFER,
                    amount = amount,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    timestamp = selectedDate.timeInMillis
                )

                when (val result = repo.saveRecord(input)) {
                    is SaveResult.Success -> {
                        val formattedAmount = String.format("%,.0f", amount)
                        SuccessDialog.show(
                            requireContext(),
                            title = "Transfer Saved",
                            message = "KSh $formattedAmount transferred from $fromAccountName to $toAccountName successfully."
                        ) {
                            try { findNavController().popBackStack() } catch (_: Exception) { amountInput.text.clear() }
                        }
                    }
                    is SaveResult.Invalid -> {
                        Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val categoryName = categorySpinner.selectedItem?.toString().orEmpty()
                if (categoryName.isBlank()) {
                    Toast.makeText(requireContext(), getString(R.string.category_error), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val categoryId = currentCategories.find { it.name.equals(categoryName, ignoreCase = true) }?.id

                val input = RecordInput(
                    type = recType,
                    amount = amount,
                    accountId = accountId,
                    categoryId = categoryId,
                    timestamp = selectedDate.timeInMillis
                )

                when (val result = repo.saveRecord(input)) {
                    is SaveResult.Success -> {
                        val formattedAmount = String.format("%,.0f", amount)
                        val typeLabel = if (recType == RecordType.INCOME) "Income" else "Expense"
                        SuccessDialog.show(
                            requireContext(),
                            title = "$typeLabel Saved",
                            message = "KSh $formattedAmount ($typeLabel) for $categoryName on $fromAccountName saved successfully."
                        ) {
                            try { findNavController().popBackStack() } catch (_: Exception) { amountInput.text.clear() }
                        }
                    }
                    is SaveResult.Invalid -> {
                        Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupKeypad(view: View, amountInput: EditText) {
        val numberButtons = listOf(
            R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
            R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9
        )

        numberButtons.forEach { id ->
            view.findViewById<Button>(id).setOnClickListener { button ->
                amountInput.append((button as Button).text.toString())
            }
        }

        view.findViewById<Button>(R.id.keyDecimal).setOnClickListener {
            val currentText = amountInput.text.toString()
            if (!currentText.contains(".")) {
                if (currentText.isEmpty()) {
                    amountInput.append("0.")
                } else {
                    amountInput.append(".")
                }
            }
        }

        view.findViewById<Button>(R.id.keyBackspace).setOnClickListener {
            val currentText = amountInput.text.toString()
            if (currentText.isNotEmpty()) {
                amountInput.text.delete(currentText.length - 1, currentText.length)
            }
        }
    }

    private fun updateTransactionType(
        type: String,
        incomeButton: Button,
        expenseButton: Button,
        transferButton: Button,
        categorySpinner: Spinner,
        tvAccountLabel: TextView,
        layoutCategory: View,
        layoutToAccount: View,
        repo: PockitRepository
    ) {
        transactionType = type

        val selectedColor = 0xFFE4D1F1.toInt()
        val unselectedColor = 0xFF4E285E.toInt()
        val selectedTextColor = 0xFF4E285E.toInt()
        val unselectedTextColor = 0xFFFFFFFF.toInt()

        val buttons = listOf(
            incomeButton to "Income",
            expenseButton to "Expense",
            transferButton to "Transfer"
        )

        buttons.forEach { (button, buttonType) ->
            val selected = buttonType == type
            button.setBackgroundTintList(ColorStateList.valueOf(if (selected) selectedColor else unselectedColor))
            button.setTextColor(if (selected) selectedTextColor else unselectedTextColor)
        }

        if (type == "Transfer") {
            tvAccountLabel.text = "From Account"
            layoutCategory.visibility = View.GONE
            layoutToAccount.visibility = View.VISIBLE
        } else {
            tvAccountLabel.text = "Account"
            layoutCategory.visibility = View.VISIBLE
            layoutToAccount.visibility = View.GONE

            val recType = if (type == "Income") RecordType.INCOME else RecordType.EXPENSE
            lifecycleScope.launch {
                val dbCategories = repo.observeCategories(recType).firstOrNull() ?: emptyList()
                currentCategories = dbCategories
                val catNames = if (dbCategories.isNotEmpty()) {
                    dbCategories.map { it.name }
                } else {
                    val resArray = if (type == "Income") R.array.income_categories else R.array.expense_categories
                    resources.getStringArray(resArray).toList()
                }

                val catAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    catNames
                ).apply {
                    setDropDownViewResource(R.layout.spinner_dropdown_item)
                }
                categorySpinner.adapter = catAdapter
            }
        }
    }

    private fun updateDateButton(button: Button) {
        button.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.time)
    }

    private fun updateTimeText(textView: TextView) {
        textView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
    }
}
