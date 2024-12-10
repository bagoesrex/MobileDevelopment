package com.example.skincure.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    /**
     * Fungsi untuk memformat timestamp ke dalam format tanggal tertentu
     * @param timestamp Nilai waktu dalam milidetik (Long)
     * @param format Format yang diinginkan (default: "yyyy-MM-dd HH:mm:ss")
     * @return String hasil format tanggal
     */
    fun formatTimestamp(timestamp: Long, format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Fungsi untuk mendapatkan tanggal saat ini dalam format tertentu
     * @param format Format yang diinginkan (default: "yyyy-MM-dd")
     * @return String hasil format tanggal saat ini
     */
    fun getCurrentDate(format: String = "yyyy-MM-dd"): String {
        val currentDate = Date()
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(currentDate)
    }
}
