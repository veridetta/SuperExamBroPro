package com.vr.superexambropro.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.vr.superexambropro.AboutActivity
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.login.LoginActivity
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.activity.siswa.SiswaActivity
import com.vr.superexambropro.helper.initNoInternetLayout
import com.vr.superexambropro.helper.isInternetAvailable

class RoleActivity : AppCompatActivity() {
    //init elemen
    lateinit var cardSiswa : CardView
    lateinit var cardGuru : CardView
    lateinit var btnAbout : LinearLayout
    lateinit var btnExit : LinearLayout
    var isKoneksi: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role)
        initView()
        elementClick()
    }

    private fun initView(){
        cardSiswa = findViewById(R.id.card_siswa)
        cardGuru = findViewById(R.id.card_guru)
        initNoInternetLayout(this, R.id.noInternet)
        isKoneksi = isInternetAvailable(this, R.id.noInternet)
        btnAbout = findViewById(R.id.btnAbout)
        btnExit = findViewById(R.id.btnExit)

    }
    private fun elementClick(){
        cardSiswa.setOnClickListener {
            //pindah ke activity siswa
            intent = Intent(this, SiswaActivity::class.java)
            startActivity(intent)
        }
        cardGuru.setOnClickListener {
            //pindah ke activity guru
            //cek sudah login belum
            val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val isLogin = sharedPreferences.getBoolean("isLogin", false)
            if (isLogin){
                //jika sudah login
                intent = Intent(this, GuruActivity::class.java)
                startActivity(intent)
            }else{
                //jika belum login
                intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        btnAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
        btnExit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Konfirmasi")
            builder.setMessage("Apakah Anda yakin ingin keluar?")
            builder.setPositiveButton("Ya") { dialogInterface: DialogInterface, i: Int ->
                // Jika pengguna memilih "Ya", keluar dari aplikasi
                finishAffinity()
            }
            builder.setNegativeButton("Tidak") { dialogInterface: DialogInterface, i: Int ->
                // Jika pengguna memilih "Tidak", tutup dialog
                dialogInterface.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }
}