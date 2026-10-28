package com.group.pockit.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.group.pockit.R
import com.group.pockit.databinding.FragmentDashboardBinding
import java.time.LocalTime

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val b get() = _binding!!

    private val vm: DashboardViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    private val DUMMY_NAME = "User"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TransactionAdapter()
        b.rvRecent.layoutManager = LinearLayoutManager(context)
        b.rvRecent.adapter = adapter

        val hour = LocalTime.now().hour
        b.tvGreeting.text = when {
            hour < 12 -> "Good Morning,"
            hour < 17 -> "Good Afternoon,"
            else -> "Good Evening,"
        }

        b.tvName.text = DUMMY_NAME

        b.fabAdd.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_dashboardFragment_to_addExpenseFragment)
        }

        b.tabDay.setOnClickListener { setTimeTab(b.tabDay) }
        b.tabWeek.setOnClickListener { setTimeTab(b.tabWeek) }
        b.tabMonth.setOnClickListener { setTimeTab(b.tabMonth) }

        b.tabExpense.setOnClickListener { setTypeTab(b.tabExpense, "Expense") }
        b.tabIncome.setOnClickListener { setTypeTab(b.tabIncome, "Income") }

        vm.state.observe(viewLifecycleOwner) { st ->
            val o = st.overview

            b.tvRemainingLabel.text = "Remaining"
            b.tvRemaining.text = "Ksh ${money(o.remaining)}"
            b.tvSpentOfLimit.text = "Spent Ksh ${money(o.spent)} of Ksh ${money(o.limit)} limit"

            val percent = if (o.limit > 0) ((o.spent / o.limit) * 100).toInt() else 0
            b.progressBudget.progress = percent
            b.tvPercentUsed.text = "$percent% used"

            if (o.over) {
                b.bannerOverBudget.visibility = View.VISIBLE
                b.bannerOverBudget.text = "Warning: you are over budget by Ksh ${money(-o.remaining)}"
            } else {
                b.bannerOverBudget.visibility = View.GONE
            }

            adapter.submitList(st.recent)
            b.tvEmpty.visibility = if (st.recent.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setTimeTab(selectedTab: TextView) {
        val purpleText = resources.getColor(R.color.pockit_purple, null)
        val whiteText = resources.getColor(R.color.p2_white, null)

        b.tabDay.setBackgroundResource(0)
        b.tabDay.setTextColor(purpleText)
        b.tabWeek.setBackgroundResource(0)
        b.tabWeek.setTextColor(purpleText)
        b.tabMonth.setBackgroundResource(0)
        b.tabMonth.setTextColor(purpleText)

        selectedTab.setBackgroundResource(R.drawable.p2_bg_pill_selected)
        selectedTab.setTextColor(whiteText)
    }

    private fun setTypeTab(selectedTab: TextView, type: String) {
        val purpleText = resources.getColor(R.color.pockit_purple, null)
        val whiteText = resources.getColor(R.color.p2_white, null)

        b.tabExpense.setBackgroundResource(0)
        b.tabExpense.setTextColor(purpleText)
        b.tabIncome.setBackgroundResource(0)
        b.tabIncome.setTextColor(purpleText)

        selectedTab.setBackgroundResource(R.drawable.p2_bg_pill_selected)
        selectedTab.setTextColor(whiteText)

        vm.setType(type)
    }

    private fun money(amount: Double): String {
        return String.format("%,.2f", amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}