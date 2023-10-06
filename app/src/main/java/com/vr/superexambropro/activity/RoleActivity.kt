package com.vr.superexambropro.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.LoginActivity
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.activity.siswa.SiswaActivity
import com.vr.superexambropro.helper.initNoInternetLayout
import com.vr.superexambropro.helper.isInternetAvailable

class RoleActivity : AppCompatActivity() {
    //init elemen
    lateinit var cardSiswa : CardView
    lateinit var cardGuru : CardView
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var isLogin: Boolean = false
    var isKoneksi: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role)
        initView()
        elementClick()
        checkLogin()
    }

    fun initView(){
        cardSiswa = findViewById(R.id.card_siswa)
        cardGuru = findViewById(R.id.card_guru)
        initNoInternetLayout(this, R.id.noInternet)
        isKoneksi = isInternetAvailable(this, R.id.noInternet)

    }
    fun elementClick(){
        cardSiswa.setOnClickListener {
            //pindah ke activity siswa
            intent = intent.setClass(this, SiswaActivity::class.java)
            startActivity(intent)
            finish()
        }
        cardGuru.setOnClickListener {
            //pindah ke activity guru
            intent = intent.setClass(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun checkLogin(){
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        isLogin = sharedPreferences.getBoolean("isLogin", false)
        if (isLogin){
            //jika sudah login
            //pindah ke main activity
            intent = intent.setClass(this, GuruActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}