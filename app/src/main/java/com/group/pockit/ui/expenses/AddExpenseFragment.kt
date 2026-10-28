package com.group.pockit.ui.expenses

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
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
        val dateButton = view.findViewById<Button>(R.id.buttonDate)
        val timeText = view.findViewById<TextView>(R.id.textTime)
        val saveButton = view.findViewById<TextView>(R.id.buttonSaveExpense)

        val incomeButton = view.findViewById<Button>(R.id.buttonIncome)
        val expenseButton = view.findViewById<Button>(R.id.buttonExpense)
        val transferButton = view.findViewById<Button>(R.id.buttonTransfer)
        val cancelButton = view.findViewById<TextView>(R.id.buttonCancel)

        amountInput.showSoftInputOnFocus = false

        setupKeypad(view, amountInput)

        updateDateButton(dateButton)
        updateTimeText(timeText)

        // Ensure account spinner text color is black
        accountSpinner.adapter = object : ArrayAdapter<CharSequence>(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.expense_accounts)) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as? TextView)?.setTextColor(0xFF000000.toInt())
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as? TextView)?.setTextColor(0xFF000000.toInt())
                return v
            }
        }

        updateTransactionType(
            "Expense",
            incomeButton,
            expenseButton,
            transferButton,
            categorySpinner
        )

        incomeButton.setOnClickListener {
            updateTransactionType(
                "Income",
                incomeButton,
                expenseButton,
                transferButton,
                categorySpinner
            )
        }

        expenseButton.setOnClickListener {
            updateTransactionType(
                "Expense",
                incomeButton,
                expenseButton,
                transferButton,
                categorySpinner
            )
        }

        transferButton.setOnClickListener {
            updateTransactionType(
                "Transfer",
                incomeButton,
                expenseButton,
                transferButton,
                categorySpinner
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

            Toast.makeText(
                requireContext(),
                "Form cleared",
                Toast.LENGTH_SHORT
            ).show()
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

            val date = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(selectedDate.time)

            val time = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Calendar.getInstance().time)

            if (category.isBlank() || account.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.category_error),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val message = "${getString(R.string.transaction_validated)}: " +
                    "$transactionType, $account, $category, KSh $amount, $date $time"

            Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupKeypad(view: View, amountInput: EditText) {

        val numberButtons = listOf(
            R.id.key0,
            R.id.key1,
            R.id.key2,
            R.id.key3,
            R.id.key4,
            R.id.key5,
            R.id.key6,
            R.id.key7,
            R.id.key8,
            R.id.key9
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
                amountInput.text.delete(
                    currentText.length - 1,
                    currentText.length
                )
            }
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

        // Inverted: Selected is Lavender (#E4D1F1) with dark text (#4E285E), Unselected is Dark Purple (#4E285E) with white text
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

            button.setBackgroundTintList(
                ColorStateList.valueOf(
                    if (selected) selectedColor else unselectedColor
                )
            )

            button.setTextColor(
                if (selected) selectedTextColor else unselectedTextColor
            )
        }

        val categories = when (type) {
            "Income" -> R.array.income_categories
            "Transfer" -> R.array.transfer_categories
            else -> R.array.expense_categories
        }

        val catArray = resources.getStringArray(categories)
        categorySpinner.adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, catArray) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as? TextView)?.setTextColor(0xFF000000.toInt())
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as? TextView)?.setTextColor(0xFF000000.toInt())
                return v
            }
        }
    }

    private fun updateDateButton(button: Button) {
        button.text = SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        ).format(selectedDate.time)
    }

    private fun updateTimeText(textView: TextView) {
        textView.text = SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(Calendar.getInstance().time)
    }
}
