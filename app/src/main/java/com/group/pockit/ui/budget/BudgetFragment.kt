package com.group.pockit.ui.budget

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group.pockit.R
import com.group.pockit.databinding.FragmentBudgetBinding
import kotlin.math.roundToInt

// One category: how much was spent and its monthly limit (null = no limit set yet)
data class BudgetCategory(val name: String, val spent: Double, var limit: Double?)

private fun ksh(v: Double) = String.format("%,.0f", v)

private const val RED = 0xFFE8112D.toInt()
private const val AMBER = 0xFFD4A000.toInt()
private const val PURPLE = 0xFF7B2D8C.toInt()

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val b get() = _binding!!

    // Sample data until the database is connected
    private val categories = mutableListOf(
        BudgetCategory("Shopping", 3500.0, 3000.0),
        BudgetCategory("Food", 3450.0, 4000.0),
        BudgetCategory("Transport", 200.0, 1000.0),
        BudgetCategory("Bills", 2500.0, 3500.0),
        BudgetCategory("Entertainment", 1200.0, null)
    )
    private var customTotal: Double? = null   // set with "Edit total budget"
    private lateinit var adapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.tvMonth.text = "Oct 2026"
        b.tvRemainingLabel.text = "Left to spend this month"

        adapter = BudgetAdapter(categories) { index ->
            val cat = categories[index]
            askAmount("Monthly limit for ${cat.name}") { amount ->
                cat.limit = amount
                refresh()
            }
        }
        b.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        b.rvCategories.adapter = adapter

        b.btnEditTotal.setOnClickListener {
            askAmount("Total monthly budget") { amount ->
                customTotal = amount
                refresh()
            }
        }

        refresh()
    }

    // Recalculates the purple card and redraws the list
    private fun refresh() {
        val spent = categories.sumOf { it.spent }
        val limit = customTotal ?: categories.sumOf { it.limit ?: 0.0 }
        val remaining = limit - spent
        val percent = if (limit > 0) (spent / limit * 100).roundToInt() else 0

        b.tvRemaining.text =
            if (remaining >= 0) "KSh ${ksh(remaining)}" else "KSh ${ksh(-remaining)} over"
        b.tvSpentOfLimit.text = "Spent KSh ${ksh(spent)} of KSh ${ksh(limit)} limit"
        b.tvPercentUsed.text = "$percent% used"
        b.progressBudget.progress = percent.coerceAtMost(100)
        b.progressBudget.progressTintList = ColorStateList.valueOf(0xFFFFD400.toInt())
        b.progressBudget.progressBackgroundTintList = ColorStateList.valueOf(0x55FFFFFF)

        adapter.notifyDataSetChanged()
    }

    // Small popup with a number box
    private fun askAmount(title: String, onSave: (Double) -> Unit) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Amount in KSh"
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().toDoubleOrNull()
                if (value != null && value > 0) onSave(value)
                else Toast.makeText(requireContext(), "Enter an amount above 0", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class BudgetAdapter(
    private val items: List<BudgetCategory>,
    private val onSetBudget: (Int) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvName)
        val spentOf: TextView = v.findViewById(R.id.tvSpentOf)
        val status: TextView = v.findViewById(R.id.tvStatus)
        val bar: ProgressBar = v.findViewById(R.id.pbCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_budget_category, parent, false)
    )

    override fun onBindViewHolder(h: VH, position: Int) {
        val cat = items[position]
        h.name.text = cat.name
        h.bar.progressBackgroundTintList = ColorStateList.valueOf(0xFFF3EEF6.toInt())

        val limit = cat.limit
        if (limit == null) {
            // No limit yet: show a "Set budget" button
            h.spentOf.text = "KSh ${ksh(cat.spent)} spent · no limit"
            h.status.text = "Set budget"
            h.status.setTextColor(0xFF46044B.toInt())
            h.status.background = GradientDrawable().apply {
                cornerRadius = 60f
                setStroke(3, 0xFF46044B.toInt())
            }
            h.status.setOnClickListener { onSetBudget(h.bindingAdapterPosition) }
            h.bar.visibility = View.GONE
            return
        }

        val ratio = cat.spent / limit
        val color = when {
            ratio > 1.0 -> RED
            ratio >= 0.8 -> AMBER
            else -> PURPLE
        }
        h.spentOf.text = "KSh ${ksh(cat.spent)} of KSh ${ksh(limit)}"
        h.status.text = if (cat.spent > limit) "KSh ${ksh(cat.spent - limit)} over"
        else "KSh ${ksh(limit - cat.spent)} left"
        h.status.setTextColor(color)
        h.status.background = null
        h.status.setOnClickListener { onSetBudget(h.bindingAdapterPosition) } // tap to change the limit
        h.bar.visibility = View.VISIBLE
        h.bar.progress = (ratio * 100).roundToInt().coerceAtMost(100)
        h.bar.progressTintList = ColorStateList.valueOf(color)
    }

    override fun getItemCount() = items.size
}