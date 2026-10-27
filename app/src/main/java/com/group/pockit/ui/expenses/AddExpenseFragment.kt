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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amountInput = view.findViewById<EditText>(R.id.inputAmount)
        val categorySpinner = view.findViewById<Spinner>(R.id.spinnerCategory)
        val noteInput = view.findViewById<EditText>(R.id.inputNote)
        val dateButton = view.findViewById<Button>(R.id.buttonDate)
        val saveButton = view.findViewById<Button>(R.id.buttonSaveExpense)

        updateDateButton(dateButton)

        dateButton.setOnClickListener {
            val today = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    selectedDate.set(year, month, day)
                    updateDateButton(dateButton)
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        saveButton.setOnClickListener {
            val amountText = amountInput.text.toString().trim()
            val note = noteInput.text.toString().trim()

            val amount = amountText.toDoubleOrNull()

            if (amount == null || amount <= 0.0) {
                amountInput.error = "Enter a valid amount greater than zero"
                return@setOnClickListener
            }

            val category = categorySpinner.selectedItem?.toString().orEmpty()
            val date = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(selectedDate.time)

            Toast.makeText(
                requireContext(),
                "Expense details validated: $category, $date",
                Toast.LENGTH_SHORT
            ).show()

            // Database saving will be connected when the team's
            // database/repository implementation is ready.
        }
    }

    private fun updateDateButton(button: Button) {
        button.text = SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        ).format(selectedDate.time)
    }
}