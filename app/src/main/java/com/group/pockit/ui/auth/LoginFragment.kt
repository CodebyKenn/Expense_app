package com.group.pockit.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group.pockit.R
import com.group.pockit.data.Session

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            when {
                email.isEmpty() -> etEmail.error = "Enter your email"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    etEmail.error = "Enter a valid email"
                password.isEmpty() -> etPassword.error = "Enter your password"
                else -> {
                    // Set session userId and display name from email
                    Session.userId = email.lowercase().trim()
                    val userPrefix = email.substringBefore("@")
                    Session.displayName = userPrefix.replaceFirstChar { it.uppercase() }
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
            }
        }

        tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }
}
