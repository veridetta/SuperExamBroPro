package com.vr.superexambropro.activity.guru.auth.fragment

import android.app.ProgressDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.helper.generateRandomString
import com.vr.superexambropro.helper.getCurrentDate
import com.vr.superexambropro.helper.showSnackBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btnAdd: Button

    private lateinit var etNamaUjian: EditText
    private lateinit var etMapel: EditText
    private lateinit var etKelas: EditText
    private lateinit var etDurasi: EditText
    private lateinit var etUrl: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        initView(itemView)
        initClick()
    }
    private fun initView(itemView: View){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        btnAdd = itemView.findViewById(R.id.btnAdd)
        etNamaUjian = itemView.findViewById(R.id.etNamaUjian)
        etKelas = itemView.findViewById(R.id.etKelas)
        etMapel = itemView.findViewById(R.id.etMapel)
        etDurasi = itemView.findViewById(R.id.etDurasi)
        etUrl = itemView.findViewById(R.id.etUrl)
    }
    private fun initClick(){
        btnAdd.setOnClickListener {
            val namaUjian = etNamaUjian.text.toString()
            val mapel = etMapel.text.toString()
            val kelas = etKelas.text.toString()
            val durasi = etDurasi.text.toString()
            val url = etUrl.text.toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (namaUjian.isNotEmpty() && mapel.isNotEmpty() && kelas.isNotEmpty() && durasi.isNotEmpty()
                && url.isNotEmpty()) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(context)
                progressDialog.setMessage("Menyimpan data...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Tambahkan detail produk ke Firestore
                    addDataToFirestore(
                        namaUjian,
                        mapel,
                        kelas,
                        durasi,
                        url
                    )
                    progressDialog.dismiss()
                }
            } else {
                showSnackBar(requireView(),"Mohon isi semua field yang diperlukan")
            }
        }
    }

    private fun addDataToFirestore(
        namaUjian: String,
        mapel: String,
        kelas: String,
        durasi: String,
        url : String
    ) {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "")
        val currentUser = FirebaseAuth.getInstance().currentUser
        val barangData = hashMapOf(
            "uid" to UUID.randomUUID().toString(),
            "userId" to currentUser!!.uid,
            "namaUjian" to namaUjian,
            "mapel" to mapel,
            "kelas" to kelas,
            "namaGuru" to userName,
            "url" to url,
            "shortUrl" to generateRandomString(8),
            "key" to generateRandomString(8),
            "durasi" to durasi,
            "status" to "Belum",
            "created_at" to getCurrentDate(),
        )

        val db = FirebaseFirestore.getInstance()

        // Add the product data to Firestore
        db.collection("paket")
            .add(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnackBar(requireView(),"Berhasil menyimpan data")
                // Redirect to SellerActivity fragment home
                val intent = Intent(requireActivity(), GuruActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                showSnackBar(requireView(),"Gagal menyimpan data ${e.message}")
            }
    }
}