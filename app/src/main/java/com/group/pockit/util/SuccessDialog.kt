package com.group.pockit.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.group.pockit.R

object SuccessDialog {

    fun show(
        context: Context,
        title: String = "Success",
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_success, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvDialogMessage)
        val btnCloseX = view.findViewById<View>(R.id.btnCloseX)
        val btnDone = view.findViewById<Button>(R.id.btnDialogDone)

        tvTitle.text = title
        tvMessage.text = message

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dismissAction = {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            onDismiss?.invoke()
        }

        btnCloseX.setOnClickListener { dismissAction() }
        btnDone.setOnClickListener { dismissAction() }
        dialog.setOnCancelListener { onDismiss?.invoke() }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }
}
