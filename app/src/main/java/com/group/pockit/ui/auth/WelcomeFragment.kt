package com.group.pockit.ui.auth


import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group.pockit.R

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnCreateAccount).setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_register)
        }

        view.findViewById<Button>(R.id.btnLogIn).setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_login)
        }
    }
}
