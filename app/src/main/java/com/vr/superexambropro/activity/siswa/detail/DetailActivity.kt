package com.vr.superexambropro.activity.siswa.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.ujian.UjianVM
import com.vr.superexambropro.activity.siswa.MulaiActivity
import com.vr.superexambropro.databinding.ActivityDetailBinding
import com.vr.superexambropro.databinding.ActivityUjianBinding
import com.vr.superexambropro.helper.addMinutesToCurrentDate
import com.vr.superexambropro.helper.generateRandomString
import com.vr.superexambropro.helper.getCurrentDate
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.model.PaketModel
import com.vr.superexambropro.model.UjianModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DetailActivity : AppCompatActivity() {
    private lateinit var vm: DetailVM
    private lateinit var binding: ActivityDetailBinding

    private val mFirestore = FirebaseFirestore.getInstance()


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivity()
        initClick()
        initIntent()
        observe()
        //jangan lupa awal-awal buat fungsi untuk cek data ada atau tidak, jika tidak ada balikan ke halamn sebelumnya
        //rencana tampil iklan sebelum mengerjakan
    }
    private fun initActivity(){
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[DetailVM::class.java]
    }

    private fun initClick(){
        binding.btnMulai.setOnClickListener{
            insertData()
        }
    }
    private fun initIntent(){
        namaSiswa = intent.getStringExtra("nama").toString()
        shortUrl = intent.getStringExtra("kode").toString()
    }

    private fun initData(){
        binding.tvNamaUjian.text = ": $namaUjian"
        binding.tvMapel.text = ": $mapel"
        binding.tvKelas.text = ": $kelas"
        binding.tvDurasi.text = ": $durasi Menit"
        binding.tvGuru.text = ": $guru"
    }

    private fun observe(){
        vm.data.observe(this){
            if (it.isNotEmpty()){
                namaUjian = it[0].namaUjian.toString()
                mapel = it[0].mapel.toString()
                durasi = it[0].durasi.toString()
                kelas = it[0].kelas.toString()
                guru = it[0].namaGuru.toString()
                url = it[0].url.toString()
                paketId = it[0].uid.toString()
                initData()
            }
        }
        vm.getData(mFirestore, this,shortUrl)
    }

    private fun insertData(){

        val data = UjianModel()
        data.paketId = paketId
        data.namaSiswa = namaSiswa
        data.waktuMulai = getCurrentDate()
        data.waktuSelesai = addMinutesToCurrentDate(durasi.toInt())
        data.status = "Sedang Mengerjakan"
        data.kodeKeamanan = generateRandomString(6)
        data.durasi = durasi
        data.created_at = getCurrentDate()

        vm.doInsert(mFirestore, this,data)
        vm.docId.observe(this){
            if (it.isNotEmpty()){
                val intent = Intent(this, MulaiActivity::class.java)
                intent.putExtra("test_lock", true)
                intent.putExtra("documentId",it)
                intent.putExtra("namaUjian",namaUjian)
                intent.putExtra("mapel",mapel)
                intent.putExtra("durasi",durasi)
                intent.putExtra("kelas",kelas)
                intent.putExtra("guru",guru)
                intent.putExtra("url",url)
                intent.putExtra("kode",data.kodeKeamanan)
                startActivity(intent)
                finish()
            }
        }
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