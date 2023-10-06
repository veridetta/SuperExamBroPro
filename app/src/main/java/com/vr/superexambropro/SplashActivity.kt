package com.vr.superexambropro

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
        if (isKoneksi){
            //jika ada koneksi
            lyNokonek.visibility = CoordinatorLayout.GONE
            xprogressBar()
           // checkLogin()
        }else{
            //jika tidak ada koneksi
            lyNokonek.visibility = CoordinatorLayout.VISIBLE
            showSnackBar(contentView, "Tidak ada koneksi internet")
        }
    }

    fun initView(){
        progressBar = findViewById(R.id.progressBar)
        contentView = findViewById(R.id.contentView)
        lyNokonek = findViewById(R.id.noInternet)
        //initNoInternetLayout(this, R.id.noInternet)
    }
    fun checkKoneksi(){
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkInfo = connectivityManager.getNetworkInfo(network)
        if (networkInfo == null || !networkInfo.isConnected || !networkInfo.isAvailable) {
            isKoneksi = false
        } else {
            isKoneksi = true
        }
    }
    fun checkLogin(){
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        isLogin = sharedPreferences.getBoolean("isLogin", false)
        if (isLogin){
            //jika sudah login
            progressBar.setProgress(100)
            intent = intent.setClass(this, GuruActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            //jika belum login
            progressBar.setProgress(100)
            intent = intent.setClass(this, RoleActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun xprogressBar() {
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
                    checkLogin()
                    progressBar.setProgress(n)
                }
            }
        }).start()
    }

}