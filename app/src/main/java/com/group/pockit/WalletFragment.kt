package com.group.pockit

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class WalletFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create a simple layout via code so there are no XML errors
        val layout = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#F8F1DC")) // Your cream color
        }

        val text = TextView(context).apply {
            this.text = "Wallet is Working!"
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#46044B")) // Your purple color
        }

        layout.addView(text)
        (view as ViewGroup).addView(layout)
    }
}