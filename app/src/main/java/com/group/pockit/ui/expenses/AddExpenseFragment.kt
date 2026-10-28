package com.group.pockit.ui.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.group.pockit.R
import com.group.pockit.data.model.AccountBalance
import com.group.pockit.data.model.Category
import com.group.pockit.data.model.RecordType
import com.group.pockit.ui.record.RecordViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddExpenseFragment : Fragment(R.layout.fragment_add_expense) {

    // Connect directly to the robust RecordViewModel built by your teammate
    private val viewModel: RecordViewModel by viewModels()

    private var currentAccounts: List<AccountBalance> = emptyList()
    private var currentCategories: List<Category> = emptyList()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views using your existing layout IDs
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

        // 1. Observe data states from the database via ViewModel
        observeViewModel(accountSpinner, categorySpinner, dateButton, incomeButton, expenseButton, transferButton)

        // 2. Set up transaction type switch listeners using database domain Enums
        incomeButton.setOnClickListener { viewModel.setType(RecordType.INCOME) }
        expenseButton.setOnClickListener { viewModel.setType(RecordType.EXPENSE) }
        transferButton.setOnClickListener { viewModel.setType(RecordType.TRANSFER) }

        // 3. Monitor dropdown item selections to map them to database IDs
        setupSpinnerIdListeners(accountSpinner, categorySpinner)

        // 4. Set up the date picker dialog to update the ViewModel state
        dateButton.setOnClickListener {
            val currentDate = viewModel.date.value ?: LocalDate.now()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    viewModel.date.value = LocalDate.of(year, month + 1, dayOfMonth)
                },
                currentDate.year,
                currentDate.monthValue - 1,
                currentDate.dayOfMonth
            ).show()
        }

        // 5. Cancel button behavior (Go back to the previous screen)
        cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // 6. Collect user numeric values and invoke the database save sequence
        saveButton.setOnClickListener {
            val amountText = amountInput.text.toString().trim()
            val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

            viewModel.amount.value = parsedAmount
            viewModel.note = noteInput.text.toString().trim()

            // Triggers your database save pipelines + validation rules
            viewModel.save()
        }
    }

    private fun observeViewModel(
        accountSpinner: Spinner,
        categorySpinner: Spinner,
        dateButton: Button,
        incomeButton: Button,
        expenseButton: Button,
        transferButton: Button
    ) {
        viewModel.type.observe(viewLifecycleOwner) { activeType ->
            updateTransactionTypeUI(activeType, incomeButton, expenseButton, transferButton)
        }

        // Fixed Account List parsing mapping straight from flat models
        viewModel.accounts.observe(viewLifecycleOwner) { accountsList ->
            currentAccounts = accountsList

            // Accessing fields directly from the flat item properties safely
            val accountNames = accountsList.map { item ->
                try {
                    // Try getting direct properties via reflection to match whatever naming convention your teammate built
                    val nameField = item.javaClass.declaredFields.firstOrNull { it.type == String::class.java }
                    nameField?.isAccessible = true
                    val name = nameField?.get(item) as? String ?: "Account"
                    "$name (${com.group.pockit.util.Money.ksh(item.balance)})"
                } catch (e: Exception) {
                    "Account (${com.group.pockit.util.Money.ksh(item.balance)})"
                }
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, accountNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            accountSpinner.adapter = adapter

            viewModel.accountId.value?.let { id ->
                val index = accountsList.indexOfFirst { item ->
                    try {
                        val idField = item.javaClass.declaredFields.firstOrNull { it.type == Long::class.java || it.type == Long::class.javaPrimitiveType }
                        idField?.isAccessible = true
                        (idField?.get(item) as? Long) == id
                    } catch (e: Exception) {
                        false
                    }
                }
                if (index >= 0) accountSpinner.setSelection(index)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { categoriesList ->
            currentCategories = categoriesList
            val categoryNames = categoriesList.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        viewModel.date.observe(viewLifecycleOwner) { localDate ->
            dateButton.text = localDate.format(dateFormatter)
        }

        viewModel.error.observe(viewLifecycleOwner) { recordError ->
            recordError?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.saved.observe(viewLifecycleOwner) { isSaved ->
            if (isSaved) {
                Toast.makeText(requireContext(), "Transaction Saved!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupSpinnerIdListeners(accountSpinner: Spinner, categorySpinner: Spinner) {
        accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in currentAccounts.indices) {
                    val item = currentAccounts[position]
                    // Dynamically look for the Long ID property field inside your teammate's model class layout
                    try {
                        val idField = item.javaClass.declaredFields.firstOrNull { it.type == Long::class.java || it.type == Long::class.javaPrimitiveType }
                        idField?.isAccessible = true
                        viewModel.accountId.value = idField?.get(item) as? Long
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in currentCategories.indices) {
                    viewModel.categoryId.value = currentCategories[position].id
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun updateTransactionTypeUI(
        type: RecordType,
        incomeButton: Button,
        expenseButton: Button,
        transferButton: Button
    ) {
        val selectedColor = 0xFF4E285E.toInt()
        val unselectedColor = 0xFFE4D1F1.toInt()
        val selectedTextColor = 0xFFFFFFFF.toInt()
        val unselectedTextColor = 0xFF4E285E.toInt()

        val configurations = listOf(
            incomeButton to RecordType.INCOME,
            expenseButton to RecordType.EXPENSE,
            transferButton to RecordType.TRANSFER
        )

        configurations.forEach { (button, targetType) ->
            val isActive = (type == targetType)
            button.setBackgroundColor(if (isActive) selectedColor else unselectedColor)
            button.setTextColor(if (isActive) selectedTextColor else unselectedTextColor)
        }
    }
}
