package com.vr.superexambropro.activity.guru.auth.edit

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.activity.guru.login.LoginVM
import com.vr.superexambropro.databinding.ActivityEditBinding
import com.vr.superexambropro.databinding.ActivityLoginBinding
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.PaketModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditActivity : AppCompatActivity() {
    private lateinit var vm: EditVM
    private lateinit var binding: ActivityEditBinding

    private  var auth= FirebaseAuth.getInstance()
    private  var firestore= FirebaseFirestore.getInstance()

    var docId = ""
    var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initActivity()
        isLoading()
        initData()
        initClick()
    }
    private fun initActivity(){
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[EditVM::class.java]
    }
    private fun initData(){
        val namaUjian = intent.getStringExtra("namaUjian")
        val mapel = intent.getStringExtra("mapel")
        val kelas = intent.getStringExtra("kelas")
        val durasi = intent.getStringExtra("durasi")
        val url = intent.getStringExtra("url")
        docId = intent.getStringExtra("documentId") ?:""
        userId = intent.getStringExtra("uid") ?:""

        binding.etNamaUjian.setText(namaUjian)
        binding.etMapel.setText(mapel)
        binding.etKelas.setText(kelas)
        binding.etDurasi.setText(durasi)
        binding.etUrl.setText(url)
    }
    private fun initClick(){
        binding.btnSimpan.setOnClickListener {
            val namaUjian = binding.etNamaUjian.text.toString()
            val mapel = binding.etMapel.text.toString()
            val kelas = binding.etKelas.text.toString()
            val durasi = binding.etDurasi.text.toString()
            val url = binding.etUrl.text.toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (namaUjian.isNotEmpty() && mapel.isNotEmpty() && kelas.isNotEmpty() && durasi.isNotEmpty()
                && url.isNotEmpty()) {
                editData(
                    namaUjian,
                    mapel,
                    kelas,
                    durasi,
                    url
                )
            } else {
                showSnackbarContext(this,"Mohon isi semua field yang diperlukan")
            }
        }
    }
    private fun editData(
        namaUjian: String,
        mapel: String,
        kelas: String,
        durasi: String,
        url : String
    ) {
        val data = PaketModel()
        data.namaUjian = namaUjian
        data.mapel = mapel
        data.kelas = kelas
        data.durasi = durasi
        data.url = url
        data.documentId=docId

        vm.updateDone.observe(this){
            if (it){
                showSnackbarContext(this,"Berhasil mengubah informasi")
                startActivity(Intent(this, GuruActivity::class.java))
                finish()
            }else{
                showSnackbarContext(this,"Gagal mengubah informasi")
            }
        }
        vm.doUpdate(firestore,this,data)
    }
    fun isLoading(){
        vm.isLoading.observe(this) {
            if (it){

                binding.ccLoading.contentLoading.visibility= View.VISIBLE
            }else{
                binding.ccLoading.contentLoading.visibility = View.GONE
            }
        }
    }
}