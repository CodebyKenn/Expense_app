package com.group.pockit.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.group.pockit.R

class WalletFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addAccountButton = view.findViewById<View>(R.id.buttonAddAccount)

        addAccountButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Add Account selected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}