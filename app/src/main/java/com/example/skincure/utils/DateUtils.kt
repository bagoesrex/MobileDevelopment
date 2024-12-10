package com.example.skincure.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun dateFormatter(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            Locale.US
        )
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat(
            "HH.mm | yyyy-MM-dd ",
            Locale.getDefault()
        )
        outputFormat.timeZone = TimeZone.getDefault()

        val date = inputFormat.parse(createdAt)
        if (date != null) {
            outputFormat.format(date)
        } else {
            "Invalid date"
        }
    } catch (e: Exception) {
        "Invalid date"
    }
}