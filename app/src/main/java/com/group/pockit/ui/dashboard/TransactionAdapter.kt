package com.group.pockit.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group.pockit.R

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private var list = listOf<Transaction>()

    fun submitList(newList: List<Transaction>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvInitial.text = item.category.take(1).uppercase()
        holder.tvCategory.text = item.category
        holder.tvSub.text = "${item.account} • ${item.date}"

        if (item.type == "Income") {
            holder.tvAmount.text = "+ Ksh ${String.format("%,.0f", item.amount)}"
            holder.tvAmount.setTextColor(holder.itemView.context.getColor(R.color.income_green))
        } else {
            holder.tvAmount.text = "- Ksh ${String.format("%,.0f", item.amount)}"
            holder.tvAmount.setTextColor(holder.itemView.context.getColor(R.color.p2_red))
        }
    }

    override fun getItemCount() = list.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tvInitial)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvSub: TextView = view.findViewById(R.id.tvSub)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }
}