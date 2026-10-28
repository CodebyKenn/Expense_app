package com.group.pockit.ui.wallet

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group.pockit.R
import com.group.pockit.data.model.AccountBalance
import com.group.pockit.ui.accounts.AccountsViewModel
import com.group.pockit.util.SuccessDialog

private fun ksh(v: Double) = String.format("%,.0f", v)

class WalletFragment : Fragment() {

    private val viewModel: AccountsViewModel by viewModels()
    private lateinit var adapter: WalletAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textTotalBalance = view.findViewById<TextView>(R.id.textTotalBalance)
        val rvAccounts = view.findViewById<RecyclerView>(R.id.rvAccounts)
        val addAccountButton = view.findViewById<View>(R.id.buttonAddAccount)
        val loadDummyDataButton = view.findViewById<View>(R.id.buttonLoadDummyData)
        val resetDataButton = view.findViewById<View>(R.id.buttonResetData)

        rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        adapter = WalletAdapter(
            onEdit = { account -> showEditAccountDialog(account) }
        )
        rvAccounts.adapter = adapter

        viewModel.totalBalance.observe(viewLifecycleOwner) { total ->
            textTotalBalance.text = "KSh ${ksh(total ?: 0.0)}"
        }

        viewModel.accounts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        addAccountButton.setOnClickListener {
            showAddAccountDialog()
        }

        loadDummyDataButton.setOnClickListener {
            viewModel.loadDemoData {
                SuccessDialog.show(
                    requireContext(),
                    title = "Dummy Data Loaded",
                    message = "Sample accounts, transactions, and budgets have been loaded successfully."
                )
            }
        }

        resetDataButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset All Data")
                .setMessage("Are you sure you want to reset all data? This will clear all transactions, custom accounts, and budgets, restoring default accounts with KSh 0 balance.")
                .setPositiveButton("Reset") { _, _ ->
                    viewModel.resetData {
                        SuccessDialog.show(
                            requireContext(),
                            title = "Data Reset",
                            message = "All transactions and custom data have been cleared. Default accounts reset to KSh 0."
                        )
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showAddAccountDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }
        val inputName = EditText(requireContext()).apply {
            hint = "Account Name (e.g., Equity Bank)"
        }
        val inputBalance = EditText(requireContext()).apply {
            hint = "Opening Balance (e.g., 5000)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        layout.addView(inputName)
        layout.addView(inputBalance)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Account")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = inputName.text.toString().trim()
                val balance = inputBalance.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty()) {
                    viewModel.addAccount(name, balance) { success ->
                        if (success) {
                            SuccessDialog.show(
                                requireContext(),
                                title = "Account Created",
                                message = "Account '$name' created successfully with opening balance KSh ${ksh(balance)}."
                            )
                        } else {
                            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Enter a valid name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditAccountDialog(account: AccountBalance) {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }
        val inputName = EditText(requireContext()).apply {
            hint = "Account Name"
            setText(account.name)
        }
        val inputBalance = EditText(requireContext()).apply {
            hint = "Account Balance in KSh"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(account.balance.toInt().toString())
        }
        layout.addView(inputName)
        layout.addView(inputBalance)

        AlertDialog.Builder(requireContext())
            .setTitle("Manage Account")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = inputName.text.toString().trim()
                val newBalance = inputBalance.text.toString().toDoubleOrNull() ?: account.balance
                if (newName.isNotEmpty()) {
                    viewModel.updateAccount(account.id, newName, newBalance) { success ->
                        if (success) {
                            SuccessDialog.show(
                                requireContext(),
                                title = "Account Updated",
                                message = "Account '$newName' updated with balance KSh ${ksh(newBalance)}."
                            )
                        }
                    }
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                showDeleteDialog(account)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(account: AccountBalance) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete ${account.name}? All associated records will also be removed.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(account.id)
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class WalletAdapter(
    private var items: List<AccountBalance> = emptyList(),
    private val onEdit: (AccountBalance) -> Unit
) : RecyclerView.Adapter<WalletAdapter.VH>() {

    fun submitList(newItems: List<AccountBalance>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvAccountName)
        val tvBalance: TextView = v.findViewById(R.id.tvAccountBalance)
        val btnOptions: View = v.findViewById(R.id.btnOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_account, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvBalance.text = "KSh ${ksh(item.balance)}"
        holder.btnOptions.setOnClickListener {
            onEdit(item)
        }
        holder.itemView.setOnClickListener {
            onEdit(item)
        }
    }

    override fun getItemCount() = items.size
}
