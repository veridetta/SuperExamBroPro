package com.vr.superexambropro.activity.siswa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.RoleActivity

class SelesaiActivity : AppCompatActivity() {
    lateinit var btnTutup : Button
    lateinit var btnHome : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selesai)
        btnTutup = findViewById(R.id.btnTutup)
        btnHome = findViewById(R.id.btnHome)
        btnTutup.setOnClickListener {
            finish()
        }
        btnHome.setOnClickListener {
            //intent ke roleActivity
            val intent = Intent(this, RoleActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}