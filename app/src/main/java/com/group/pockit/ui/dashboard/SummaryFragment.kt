package com.group.pockit.ui.dashboard

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group.pockit.R
import com.group.pockit.data.model.CategoryShare
import com.group.pockit.databinding.FragmentSummaryBinding
import com.group.pockit.ui.analysis.AnalysisViewModel
import com.group.pockit.util.Period
import kotlin.math.roundToInt

private fun ksh(v: Double) = String.format("%,.0f", v)

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val b get() = _binding!!

    private val viewModel: AnalysisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.rvCategories.layoutManager = LinearLayoutManager(requireContext())

        // Period pills: Day / Week / Month
        b.tvDay.setOnClickListener { viewModel.setPeriod(Period.DAY) }
        b.tvWeek.setOnClickListener { viewModel.setPeriod(Period.WEEK) }
        b.tvMonth.setOnClickListener { viewModel.setPeriod(Period.MONTH) }

        // Arrows: Prev / Next
        b.btnPrev.setOnClickListener { viewModel.previous() }
        b.btnNext.setOnClickListener { viewModel.next() }

        // Observe ViewModel data
        viewModel.rangeLabel.observe(viewLifecycleOwner) { label ->
            b.tvRange.text = label
        }

        viewModel.expenseTotal.observe(viewLifecycleOwner) { total ->
            val safeTotal = total ?: 0.0
            b.tvTotal.text = "KSh ${ksh(safeTotal)}"
        }

        viewModel.period.observe(viewLifecycleOwner) { currentPeriod ->
            val tabs = listOf(b.tvDay, b.tvWeek, b.tvMonth)
            val selectedTab = when (currentPeriod) {
                Period.DAY -> b.tvDay
                Period.WEEK -> b.tvWeek
                Period.MONTH -> b.tvMonth
                else -> b.tvMonth
            }
            tabs.forEach { tab ->
                if (tab == selectedTab) {
                    tab.background = GradientDrawable().apply {
                        cornerRadius = dp(20).toFloat()
                        setColor(0xFF46044B.toInt())
                    }
                    tab.setTextColor(0xFFFFFFFF.toInt())
                } else {
                    tab.background = null
                    tab.setTextColor(0xFF46044B.toInt())
                }
            }
        }

        viewModel.expenseBreakdown.observe(viewLifecycleOwner) { items ->
            val total = items.sumOf { it.total }
            b.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            b.rvCategories.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE

            if (items.isNotEmpty()) {
                val slices = items.mapIndexed { i, item ->
                    (item.total / total).toFloat() to ChartColors.at(i)
                }
                b.donut.setData(slices, "Total spent", "KSh ${ksh(total)}")

                b.legend.removeAllViews()
                items.forEachIndexed { i, item ->
                    b.legend.addView(legendRow(item.name, ChartColors.at(i)))
                }

                b.rvCategories.adapter = DbSummaryAdapter(items, total)
            } else {
                b.donut.setData(emptyList(), "Total spent", "KSh 0")
                b.legend.removeAllViews()
            }
        }
    }

    private fun legendRow(name: String, color: Int): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
        }
        val box = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(dp(16), dp(16)).apply { topMargin = dp(2) }
            setBackgroundColor(color)
        }
        val label = TextView(requireContext()).apply {
            text = name
            textSize = 14f
            setTextColor(0xFF46044B.toInt())
            setPadding(dp(10), 0, 0, 0)
        }
        row.addView(box)
        row.addView(label)
        return row
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).roundToInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class DbSummaryAdapter(
    private val items: List<CategoryShare>,
    private val total: Double
) : RecyclerView.Adapter<DbSummaryAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val dot: View = v.findViewById(R.id.vDot)
        val name: TextView = v.findViewById(R.id.tvName)
        val percent: TextView = v.findViewById(R.id.tvPercent)
        val amount: TextView = v.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_category_summary, parent, false)
    )

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        val color = ChartColors.at(position)
        h.dot.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        h.name.text = item.name
        val pct = if (total > 0) (item.total / total * 100).roundToInt() else 0
        h.percent.text = "• $pct%"
        h.percent.setTextColor(color)
        h.amount.text = "- KSh ${ksh(item.total)}"
    }

    override fun getItemCount() = items.size
}
