package com.vr.superexambropro

import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.RoleActivity
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.helper.initNoInternetLayout
import com.vr.superexambropro.helper.isInternetAvailable
import com.vr.superexambropro.helper.showSnackBar


class SplashActivity : AppCompatActivity() {
    // This is the splash screen activity
    //lateinit
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var contentView: CoordinatorLayout
    lateinit var progressBar : TextRoundCornerProgressBar
    lateinit var lyNokonek : CoordinatorLayout
    var isLogin: Boolean = false
    var isKoneksi: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
        initView()
        checkKoneksi()
    }

    private fun initView(){
        progressBar = findViewById(R.id.progressBar)
        contentView = findViewById(R.id.contentView)
        lyNokonek = findViewById(R.id.noInternet)
        //initNoInternetLayout(this, R.id.noInternet)
    }
    private fun checkKoneksi(){
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkInfo = connectivityManager.getNetworkInfo(network)
        isKoneksi = !(networkInfo == null || !networkInfo.isConnected || !networkInfo.isAvailable)
        if (isKoneksi){
            //jika ada koneksi
            lyNokonek.visibility = CoordinatorLayout.GONE
            xprogressBar()
            Handler(Looper.getMainLooper()).postDelayed({
                checkLogin()
            }, 1000)
        }else{
            //jika tidak ada koneksi
            lyNokonek.visibility = CoordinatorLayout.VISIBLE
            showSnackBar(contentView, "Tidak ada koneksi internet")
        }
    }
    private fun checkLogin(){
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        isLogin = sharedPreferences.getBoolean("isLogin", false)
        if (isLogin){
            //jika sudah login
            progressBar.setProgress(100)
            intent = Intent(this, GuruActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            //jika belum login
            progressBar.setProgress(100)
            intent = Intent(this, RoleActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun xprogressBar() {
        //progress bar
        progressBar.setMax(100)
        progressBar.setProgress(0)

        Thread(Runnable {
            var n = 0
            while (progressBar.getProgress() < 98) {
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                n += 10
                runOnUiThread {
                    progressBar.setProgress(n)
                }
            }
        }).start()
    }

}