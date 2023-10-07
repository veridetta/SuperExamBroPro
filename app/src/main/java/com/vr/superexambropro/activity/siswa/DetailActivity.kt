package com.vr.superexambropro.activity.siswa

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.helper.addMinutesToCurrentDate
import com.vr.superexambropro.helper.generateRandomString
import com.vr.superexambropro.helper.getCurrentDate
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.model.PaketModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DetailActivity : AppCompatActivity() {

    lateinit var tvNamaUjian : TextView
    lateinit var tvMapel : TextView
    lateinit var tvKelas : TextView
    lateinit var tvDurasi : TextView
    lateinit var tvGuru : TextView
    lateinit var btnMulai : Button
    lateinit var lyLoad : CoordinatorLayout
    lateinit var contentView : CoordinatorLayout

    var namaUjian =""
    var mapel =""
    var durasi =""
    var kelas =""
    var guru =""
    val TAG = "Detail "
    var shortUrl = ""
    var namaSiswa = ""
    var paketId = ""
    var url = ""

    private val mFirestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initView()
        initClick()
        initIntent()

        getData()
        //jangan lupa awal-awal buat fungsi untuk cek data ada atau tidak, jika tidak ada balikan ke halamn sebelumnya
        //rencana tampil iklan sebelum mengerjakan
    }

    private fun initView(){
        tvNamaUjian = findViewById(R.id.tvNamaUjian)
        tvMapel = findViewById(R.id.tvMapel)
        tvKelas = findViewById(R.id.tvKelas)
        tvDurasi = findViewById(R.id.tvDurasi)
        tvGuru = findViewById(R.id.tvGuru)
        btnMulai = findViewById(R.id.btnMulai)
        contentView = findViewById(R.id.contentView)
        lyLoad = findViewById(R.id.lyLoad)
    }

    private fun initClick(){
        btnMulai.setOnClickListener{
            insertData()
        }
    }
    private fun initIntent(){
        namaSiswa = intent.getStringExtra("nama").toString()
        shortUrl = intent.getStringExtra("kode").toString()
    }

    private fun initData(){
     tvNamaUjian.text = ": $namaUjian"
     tvMapel.text = ": $mapel"
     tvKelas.text = ": $kelas"
     tvDurasi.text = ": $durasi Menit"
     tvGuru.text = ": $guru"
    }

    private fun getData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("paket").whereEqualTo("shortUrl", shortUrl).get().await()
                val datas = mutableListOf<PaketModel>()
                if(result.isEmpty){
                    showSnackBar(contentView, "Data tidak ditemukan.")
                    finish()
                }else{
                    for (document in result) {
                        val data = document.toObject(PaketModel::class.java)
                        val docId = document.id
                        data.documentId = docId
                        datas.add(data)
                        namaUjian = data.namaUjian.toString()
                        mapel = data.mapel.toString()
                        kelas = data.kelas.toString()
                        durasi = data.durasi.toString()
                        guru = data.namaGuru.toString()
                        paketId = data.uid.toString()
                        url = data.url.toString()
                        Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                    }
                    runOnUiThread {
                        lyLoad.visibility = View.GONE
                        initData()
                    }
                }

            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
            }
        }
    }

    private fun insertData(){
        val ujianData = hashMapOf(
            "uid" to UUID.randomUUID().toString(),
            "paketId" to paketId,
            "namaSiswa" to namaSiswa,
            "waktuMulai" to getCurrentDate(),
            "waktuSelesai" to addMinutesToCurrentDate(durasi.toInt()),
            "status" to "Sedang Mengerjakan",
            "kodeKeamanan" to generateRandomString(6),
            "durasi" to durasi,
            "created_at" to getCurrentDate(),
        )

        mFirestore.collection("ujian")
            .add(ujianData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnackBar(contentView,"Berhasil menyimpan data")
                //put document id
                val myIntent = Intent(applicationContext, MulaiActivity::class.java)
                myIntent.putExtra("test_lock", true)
                myIntent.putExtra("documentId", documentReference.id)
                myIntent.putExtra("url", url)
                myIntent.putExtra("durasi", ujianData["durasi"].toString())
                myIntent.putExtra("kode", ujianData["kodeKeamanan"].toString())
                startActivity(myIntent)
                //kemudian kembali normal saat selesai tes
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                showSnackBar(contentView,"Gagal menyimpan data ${e.message}")
            }
    }

}