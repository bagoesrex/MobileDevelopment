package com.example.skincure.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

@SuppressLint("ObsoleteSdkInt")
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnected
    }
}


fun showNoInternetDialog(
    fragment: Fragment,
    retryAction: () -> Unit = {
        fragment.findNavController().navigate(fragment.id)
    },
    closeAction: () -> Unit = {
        fragment.requireActivity().finish()
    }
) {
    AlertDialog.Builder(fragment.requireContext())
        .setTitle("No Internet Connection")
        .setMessage("Please check your internet connection and try again.")
        .setCancelable(false)
        .setPositiveButton("Retry") { _, _ ->
            if (!isInternetAvailable(fragment.requireContext())) {
                showNoInternetDialog(fragment, retryAction, closeAction)
            } else {
                retryAction()
            }
        }
        .setNegativeButton("Close") { _, _ ->
            closeAction()
        }
        .show()
}
