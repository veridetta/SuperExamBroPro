package com.vr.superexambropro.activity.guru.auth.ujian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.edit.EditVM
import com.vr.superexambropro.adapter.SiswaUjianAdapter
import com.vr.superexambropro.databinding.ActivityEditBinding
import com.vr.superexambropro.databinding.ActivityUjianBinding
import com.vr.superexambropro.helper.DataCallback
import com.vr.superexambropro.helper.addDocumentIdToData
import com.vr.superexambropro.helper.readDataFirebase
import com.vr.superexambropro.model.UjianModel

class UjianActivity : AppCompatActivity() {
    private lateinit var vm: UjianVM
    private lateinit var binding: ActivityUjianBinding

    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var dataAdapter: SiswaUjianAdapter
    val TAG = "LOAD DATA"
    var paketId =""
    private val dataList: MutableList<UjianModel> = mutableListOf()
    private lateinit var snapshotListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivity()
        initIntent()
        initView()
        observe()

        binding.swipeRefreshLayout.setOnRefreshListener {
            observe()
        }
    }

    private fun initActivity(){
        binding = ActivityUjianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[UjianVM::class.java]
    }
    fun initView(){
        binding.rcData.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@UjianActivity, 1)
            // set the custom adapter to the RecyclerView
            dataAdapter = SiswaUjianAdapter(
                dataList,
                this@UjianActivity
            )
        }
        binding.rcData.adapter = dataAdapter
        dataAdapter.filter("")
        binding.etCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dataAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun observe(){
        vm.data.observe(this){
            if (it.isNotEmpty()){
                dataList.clear()
                dataList.addAll(it)
                dataAdapter.notifyDataSetChanged()
                binding.tvTotalSiswa.text = ": "+it.size.toString() + " Siswa"
                var sedangMengerjakan = 0
                var selesaiMengerjakan = 0
                for (data in it){
                    if(data.status == "Sedang Mengerjakan"){
                        sedangMengerjakan += 1
                    }else if(data.status == "Selesai"){
                        selesaiMengerjakan += 1
                    }
                }
                binding.tvSedangMengerjakan.text = ": "+sedangMengerjakan.toString()+" Siswa"
                binding.tvSelesaiMengerjakan.text = ": "+selesaiMengerjakan.toString()+" Siswa"
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        vm.getData(mFirestore, this, paketId)
    }
    fun initIntent(){
        val intent = intent
        paketId = intent.getStringExtra("uid").toString()
    }
    override fun onDestroy() {
        super.onDestroy()
        // Hapus listener saat aktivitas dihancurkan
        snapshotListener.remove()
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