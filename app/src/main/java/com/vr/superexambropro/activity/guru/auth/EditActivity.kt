package com.vr.superexambropro.activity.guru.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.helper.showSnackBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var btnSimpan: Button
    private lateinit var etNamaUjian: EditText
    private lateinit var etMapel: EditText
    private lateinit var etKelas: EditText
    private lateinit var etDurasi: EditText
    private lateinit var etUrl: EditText
    private lateinit var contentView: CoordinatorLayout

    var docId = ""
    var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initView()
        initData()
        initClick()
    }
    private fun initView(){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        btnSimpan = findViewById(R.id.btnSimpan)
        etNamaUjian = findViewById(R.id.etNamaUjian)
        etKelas = findViewById(R.id.etKelas)
        etMapel = findViewById(R.id.etMapel)
        etDurasi =findViewById(R.id.etDurasi)
        etUrl = findViewById(R.id.etUrl)
        contentView = findViewById(R.id.coordinator)
    }
    private fun initData(){
        val namaUjian = intent.getStringExtra("namaUjian")
        val mapel = intent.getStringExtra("mapel")
        val kelas = intent.getStringExtra("kelas")
        val durasi = intent.getStringExtra("durasi")
        val url = intent.getStringExtra("url")
        docId = intent.getStringExtra("documentId") ?:""
        userId = intent.getStringExtra("uid") ?:""

        etNamaUjian.setText(namaUjian)
        etMapel.setText(mapel)
        etKelas.setText(kelas)
        etDurasi.setText(durasi)
        etUrl.setText(url)
    }
    private fun initClick(){
        btnSimpan.setOnClickListener {
            val namaUjian = etNamaUjian.text.toString()
            val mapel = etMapel.text.toString()
            val kelas = etKelas.text.toString()
            val durasi = etDurasi.text.toString()
            val url = etUrl.text.toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (namaUjian.isNotEmpty() && mapel.isNotEmpty() && kelas.isNotEmpty() && durasi.isNotEmpty()
                && url.isNotEmpty()) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Menyimpan data...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Tambahkan detail produk ke Firestore
                    editData(
                        namaUjian,
                        mapel,
                        kelas,
                        durasi,
                        url
                    )
                    progressDialog.dismiss()
                }
            } else {
                showSnackBar(contentView,"Mohon isi semua field yang diperlukan")
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
        val productData = hashMapOf(
            "namaUjian" to namaUjian,
            "mapel" to mapel,
            "kelas" to kelas,
            "url" to url,
            "durasi" to durasi,
        )

        val db = FirebaseFirestore.getInstance()

        db.collection("paket")
            .document(docId)
            .update(productData as Map<String, Any>)
            .addOnSuccessListener {
                // Product updated successfully
                Snackbar.make(contentView, "Informasi berhasil diubah", Snackbar.LENGTH_SHORT).show()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, GuruActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("fragment", "home")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while updating product
                Snackbar.make(contentView, "Gagal mengubah informasi: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
}