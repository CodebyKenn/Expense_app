package com.group.pockit.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group.pockit.R
import com.group.pockit.data.Session

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = view.findViewById<TextView>(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            when {
                name.isEmpty() -> etName.error = "Enter your name"
                email.isEmpty() -> etEmail.error = "Enter your email"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    etEmail.error = "Enter a valid email"
                password.length < 6 ->
                    etPassword.error = "Password must be at least 6 characters"
                password != confirmPassword ->
                    etConfirmPassword.error = "Passwords do not match"
                else -> {
                    // Save user userId and display name in Session
                    Session.userId = email.lowercase().trim()
                    Session.displayName = name
                    Toast.makeText(requireContext(), "Account created", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_register_to_dashboard)
                }
            }
        }

        tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
}
