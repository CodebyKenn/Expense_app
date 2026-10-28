package com.group.pockit.ui.dashboard


import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group.pockit.R
import com.group.pockit.databinding.FragmentSummaryBinding
import kotlin.math.roundToInt

// Sample data until the database is connected
private val SAMPLE_SPENDING = listOf(
    "Shopping" to 3500.0,
    "Food" to 3450.0,
    "Bills" to 2500.0,
    "Entertainment" to 1200.0,
    "Transport" to 200.0
)

private fun ksh(v: Double) = String.format("%,.0f", v)

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.tvRange.text = "October 2026"
        val total = SAMPLE_SPENDING.sumOf { it.second }
        b.tvTotal.text = "KSh ${ksh(total)}"

        // Donut chart: each slice = that category's share of the total
        val slices = SAMPLE_SPENDING.mapIndexed { i, (_, amount) ->
            (amount / total).toFloat() to ChartColors.at(i)
        }
        b.donut.setData(slices, "Total spent", "KSh ${ksh(total)}")

        // Legend next to the chart
        b.legend.removeAllViews()
        SAMPLE_SPENDING.forEachIndexed { i, (name, _) ->
            b.legend.addView(legendRow(name, ChartColors.at(i)))
        }

        // List of category cards
        b.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        b.rvCategories.adapter = SummaryAdapter(SAMPLE_SPENDING, total)

        // Day / Week / Month tabs (only the highlight changes for now)
        val tabs = listOf(b.tvDay, b.tvWeek, b.tvMonth)
        tabs.forEach { tab -> tab.setOnClickListener { selectTab(tab, tabs) } }
        selectTab(b.tvMonth, tabs)
    }

    private fun selectTab(selected: TextView, all: List<TextView>) {
        all.forEach { tab ->
            if (tab == selected) {
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

class SummaryAdapter(
    private val items: List<Pair<String, Double>>,
    private val total: Double
) : RecyclerView.Adapter<SummaryAdapter.VH>() {

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
        val (name, amount) = items[position]
        val color = ChartColors.at(position)
        h.dot.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
        h.name.text = name
        h.percent.text = "• ${(amount / total * 100).roundToInt()}%"
        h.percent.setTextColor(color)
        h.amount.text = "- KSh ${ksh(amount)}"
    }

    override fun getItemCount() = items.size
}