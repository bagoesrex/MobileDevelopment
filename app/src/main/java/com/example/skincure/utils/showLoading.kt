package com.example.skincure.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import com.example.skincure.R

fun createLoadingDialog(context : Context): AlertDialog {
    return AlertDialog.Builder(context)
        .setView(R.layout.loading)
        .create()
        .apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
}
