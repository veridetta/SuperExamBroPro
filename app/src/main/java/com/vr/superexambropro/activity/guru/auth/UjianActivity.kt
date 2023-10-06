package com.vr.superexambropro.activity.guru.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.adapter.SiswaUjianAdapter
import com.vr.superexambropro.adapter.UjianAdapter
import com.vr.superexambropro.helper.DataCallback
import com.vr.superexambropro.helper.readDataFirebase
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.model.PaketModel
import com.vr.superexambropro.model.UjianModel
import org.w3c.dom.Text

class UjianActivity : AppCompatActivity() {
    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var dataAdapter: SiswaUjianAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var etCari: EditText
    private lateinit var tvSedangMengerjakan: TextView
    private lateinit var tvSelesaiMengerjakan: TextView
    private lateinit var tvTotalSiswa: TextView
    val TAG = "LOAD DATA"
    var paketId =""
    private val dataList: MutableList<UjianModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ujian)
        initIntent()
        initView()
        Log.d("URL",paketId)
        readDataFirebase(mFirestore, shimmer, "ujian", "where","paketId",paketId, UjianModel::class.java, callback)
    }

    fun initView(){
        shimmer = findViewById(R.id.shimmerContainer)
        etCari = findViewById(R.id.etCari)
        tvSedangMengerjakan = findViewById(R.id.tvSedangMengerjakan)
        tvSelesaiMengerjakan = findViewById(R.id.tvSelesaiMengerjakan)
        tvTotalSiswa = findViewById(R.id.tvTotalSiswa)
        recyclerView = findViewById(R.id.rcData)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@UjianActivity, 1)
            // set the custom adapter to the RecyclerView
            dataAdapter = SiswaUjianAdapter(
                dataList,
                this@UjianActivity
            )
        }
        recyclerView.adapter = dataAdapter
        dataAdapter.filter("")
        etCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dataAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    fun initIntent(){
        val intent = intent
        paketId = intent.getStringExtra("uid").toString()
    }

    val callback = object : DataCallback<UjianModel> {
        override fun onDataLoaded(datas: List<UjianModel>) {
            // Misalnya, tambahkan datas ke adapter, dan sebagainya
            dataList.addAll(datas)
            dataAdapter.filteredDataList.addAll(datas)
            dataAdapter.notifyDataSetChanged()
            tvTotalSiswa.text = ": "+datas.size.toString() + " Siswa"
            var sedangMengerjakan = 0
            var selesaiMengerjakan = 0
            for (data in datas){
                if(data.status == "Sedang Mengerjakan"){
                    sedangMengerjakan += 1
                }else if(data.status == "Selesai"){
                    selesaiMengerjakan += 1
                }
            }
            tvSedangMengerjakan.text = ": "+sedangMengerjakan.toString()+" Siswa"
            tvSelesaiMengerjakan.text = ": "+selesaiMengerjakan.toString()+" Siswa"
        }

        override fun onError(message: String) {
            // Handle kesalahan, misalnya tampilkan pesan kesalahan kepada pengguna
            Log.d("LOAD",message)
        }
    }
}