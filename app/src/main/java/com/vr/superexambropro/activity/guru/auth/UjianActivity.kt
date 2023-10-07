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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vr.superexambropro.R
import com.vr.superexambropro.adapter.SiswaUjianAdapter
import com.vr.superexambropro.adapter.UjianAdapter
import com.vr.superexambropro.helper.DataCallback
import com.vr.superexambropro.helper.addDocumentIdToData
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
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var etCari: EditText
    private lateinit var tvSedangMengerjakan: TextView
    private lateinit var tvSelesaiMengerjakan: TextView
    private lateinit var tvTotalSiswa: TextView
    val TAG = "LOAD DATA"
    var paketId =""
    private val dataList: MutableList<UjianModel> = mutableListOf()
    private lateinit var snapshotListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ujian)
        initIntent()
        initView()
        Log.d("URL",paketId)
        readDataFirebase(mFirestore, shimmer, "ujian", "where","paketId",paketId, UjianModel::class.java, callback)
        dataListener()
         swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Di sini Anda dapat menambahkan logika untuk melakukan refresh data
            // Misalnya, mengambil data baru dari server
            // Setelah selesai, hentikan indikator refresh dengan mengatur isRefreshing ke false
            // Contoh: swipeRefreshLayout.isRefreshing = false

            // Panggil ulang fungsi untuk membaca data dari Firebase atau melakukan operasi refresh lainnya.
            readDataFirebase(mFirestore, shimmer, "ujian", "where", "paketId", paketId, UjianModel::class.java, callback)
        }
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
            swipeRefreshLayout.isRefreshing = false
        }

        override fun onError(message: String) {
            // Handle kesalahan, misalnya tampilkan pesan kesalahan kepada pengguna
            Log.d("LOAD",message)
        }
    }
    fun dataListener(){
        // Menggunakan addSnapshotListener untuk mendengarkan perubahan data secara otomatis
        snapshotListener = mFirestore.collection("ujian")
            .whereEqualTo("paketId", paketId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                // Data berubah, kita perlu membersihkan dataList dan menambahkan data yang baru
                dataList.clear()
                querySnapshot?.documents?.forEach { document ->
                    val ujianModel = document.toObject(UjianModel::class.java)
                    ujianModel?.let {
                        addDocumentIdToData(it, document.id)
                        dataList.add(it)
                    }
                }

                // Memperbarui adapter dan tampilan
                dataAdapter.notifyDataSetChanged()

                // Di sini Anda juga dapat menghitung statistik seperti tvTotalSiswa, tvSedangMengerjakan, dan tvSelesaiMengerjakan
                tvTotalSiswa.text = ": "+dataList.size.toString() + " Siswa"
                var sedangMengerjakan = 0
                var selesaiMengerjakan = 0
                for (data in dataList){
                    if(data.status == "Sedang Mengerjakan"){
                        sedangMengerjakan += 1
                    }else if(data.status == "Selesai"){
                        selesaiMengerjakan += 1
                    }
                }
                tvSedangMengerjakan.text = ": "+sedangMengerjakan.toString()+" Siswa"
                tvSelesaiMengerjakan.text = ": "+selesaiMengerjakan.toString()+" Siswa"

            }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Hapus listener saat aktivitas dihancurkan
        snapshotListener.remove()
    }
}