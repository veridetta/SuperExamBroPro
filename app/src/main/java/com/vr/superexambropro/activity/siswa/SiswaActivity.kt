package com.vr.superexambropro.activity.siswa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.RegisterActivity
import com.vr.superexambropro.helper.showSnackBar

class SiswaActivity : AppCompatActivity() {
    lateinit var btnSubmit : Button
    lateinit var etNama : EditText
    lateinit var etKode : EditText
    var nama = ""
    var kode = ""
    var isReady = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_siswa)
        initView()
        initClick()
    }

    private fun initView(){
        btnSubmit = findViewById(R.id.btnSubmit)
        etNama = findViewById(R.id.etNama)
        etKode = findViewById(R.id.etKode)
    }
    private fun initClick(){
        btnSubmit.setOnClickListener{
            if (inputCheck()){
                val intent =  Intent(this, DetailActivity::class.java)
                intent.putExtra("kode",kode)
                intent.putExtra("nama",nama)
                startActivity(intent)
            }
        }
    }
    private fun inputCheck():Boolean{
        nama = etNama.text.toString()
        kode = etKode.text.toString()
        return !(nama.equals("") && kode.equals(""))
    }
}