package com.example.skincure.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun showConfirmationDialog(
    context: Context,
    message: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(message)
        .setCancelable(false)
        .setPositiveButton("Yes") { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ ->
            onCancel()
            dialog.dismiss()
        }
    val dialog = builder.create()
    dialog.show()
}