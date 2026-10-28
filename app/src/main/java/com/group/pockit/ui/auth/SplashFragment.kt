package com.group.pockit.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group.pockit.R

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show splash for 1.2 seconds, then navigate to welcomeFragment
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                try {
                    findNavController().navigate(R.id.action_splash_to_welcome)
                } catch (e: Exception) {
                    // ignore if already navigated
                }
            }
        }, 1200)
    }
}
