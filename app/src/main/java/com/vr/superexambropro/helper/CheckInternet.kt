package com.vr.superexambropro.helper

import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.vr.superexambropro.R

fun isInternetAvailable(context: Context, viewId: Int): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val networkInfo = connectivityManager.getNetworkInfo(network)

    return if (networkInfo == null || !networkInfo.isConnected || !networkInfo.isAvailable) {
        val noInternetLayout = (context as AppCompatActivity).findViewById<CoordinatorLayout>(viewId)
        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show()
        noInternetLayout.visibility = View.VISIBLE
        false
    } else {
        true
    }
}
fun initNoInternetLayout(context: Context, viewId: Int){
    val noInternetLayout = (context as AppCompatActivity).findViewById<View>(viewId)
    val btnCheck = noInternetLayout.findViewById<Button>(R.id.btn_coba_lagi)
    val btnExit = noInternetLayout.findViewById<Button>(R.id.btn_keluar)
    noInternetLayout.visibility = View.GONE
    btnCheck.setOnClickListener {
        isInternetAvailable(context, viewId)
    }
    btnExit.setOnClickListener {
        //keluar aplikasi
        context.finish()
    }
}
