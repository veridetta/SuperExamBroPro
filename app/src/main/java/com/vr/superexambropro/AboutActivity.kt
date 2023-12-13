package com.vr.superexambropro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class AboutActivity : AppCompatActivity() {
    private lateinit var btnTentangAplikasi :Button
    private lateinit var btnPrivacy :Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initView()
        initClick()

    }

    private fun initView(){
        btnTentangAplikasi = findViewById(R.id.btn_tentang_aplikasi)
        btnPrivacy = findViewById(R.id.btn_privacy)
    }

    private fun initClick(){
        btnTentangAplikasi.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra("url", "https://sambropro.blogspot.com/p/tentang-super-exambro-pro.html")
        }
        btnPrivacy.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra("url", "https://sambropro.blogspot.com/p/privacy-policy_13.html")
        }
    }
}