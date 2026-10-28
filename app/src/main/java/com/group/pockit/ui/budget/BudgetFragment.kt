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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group.pockit.R
import com.group.pockit.databinding.FragmentBudgetBinding
import com.group.pockit.data.model.BudgetStatus
import kotlin.math.roundToInt

private fun ksh(v: Double) = String.format("%,.0f", v)

private const val RED = 0xFFE8112D.toInt()
private const val AMBER = 0xFFD4A000.toInt()
private const val PURPLE = 0xFF7B2D8C.toInt()

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val b get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var adapter: BudgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        adapter = BudgetAdapter(emptyList()) { categoryId, categoryName ->
            askAmount("Monthly limit for $categoryName") { amount ->
                viewModel.setBudget(categoryId, amount) { success ->
                    if (success) Toast.makeText(requireContext(), "Budget saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
        b.rvCategories.adapter = adapter

        viewModel.monthLabel.observe(viewLifecycleOwner) { label ->
            b.tvMonth.text = label
        }

        viewModel.overview.observe(viewLifecycleOwner) { overview ->
            b.tvRemainingLabel.text = "Left to spend this month"
            val remaining = overview.remaining
            val spent = overview.totalSpent
            val limit = overview.totalLimit
            val percent = overview.usedPercent

            b.tvRemaining.text =
                if (remaining >= 0) "KSh ${ksh(remaining)}" else "KSh ${ksh(-remaining)} over"
            b.tvSpentOfLimit.text = "Spent KSh ${ksh(spent)} of KSh ${ksh(limit)} limit"
            b.tvPercentUsed.text = "$percent% used"
            b.progressBudget.progress = percent.coerceAtMost(100)
            b.progressBudget.progressTintList = ColorStateList.valueOf(0xFFFFD400.toInt())
            b.progressBudget.progressBackgroundTintList = ColorStateList.valueOf(0x55FFFFFF)

            adapter.submitList(overview.categories)
        }

        b.btnPrev.setOnClickListener { viewModel.previousMonth() }
        b.btnNext.setOnClickListener { viewModel.nextMonth() }
    }

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
    private var items: List<BudgetStatus>,
    private val onSetBudget: (Long, String) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.VH>() {

    fun submitList(newItems: List<BudgetStatus>) {
        items = newItems
        notifyDataSetChanged()
    }

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

        val limit = cat.limitAmount
        if (limit == null) {
            h.spentOf.text = "KSh ${ksh(cat.spent)} spent · no limit"
            h.status.text = "Set budget"
            h.status.setTextColor(0xFF46044B.toInt())
            h.status.background = GradientDrawable().apply {
                cornerRadius = 60f
                setStroke(3, 0xFF46044B.toInt())
            }
            h.status.setOnClickListener { onSetBudget(cat.categoryId, cat.name) }
            h.bar.visibility = View.GONE
            return
        }

        val ratio = if (limit > 0.0) cat.spent / limit else 0.0
        val color = when {
            cat.isOver -> RED
            cat.isNearLimit -> AMBER
            else -> PURPLE
        }
        h.spentOf.text = "KSh ${ksh(cat.spent)} of KSh ${ksh(limit)}"
        h.status.text = if (cat.isOver) "KSh ${ksh(cat.spent - limit)} over"
        else "KSh ${ksh(limit - cat.spent)} left"
        h.status.setTextColor(color)
        h.status.background = null
        h.status.setOnClickListener { onSetBudget(cat.categoryId, cat.name) }
        h.bar.visibility = View.VISIBLE
        h.bar.progress = (ratio * 100).roundToInt().coerceAtMost(100)
        h.bar.progressTintList = ColorStateList.valueOf(color)
    }

    override fun getItemCount() = items.size
}
