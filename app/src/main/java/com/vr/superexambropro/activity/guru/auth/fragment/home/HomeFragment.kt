package com.vr.superexambropro.activity.guru.auth.fragment.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.edit.EditActivity
import com.vr.superexambropro.activity.guru.auth.ujian.UjianActivity
import com.vr.superexambropro.adapter.UjianAdapter
import com.vr.superexambropro.databinding.FragmentHomeBinding
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.PaketModel

class HomeFragment : Fragment() {
    private lateinit var vm: HomeVM
    private lateinit var binding: FragmentHomeBinding

    private val mFirestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var dataAdapter: UjianAdapter
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val dataList: MutableList<PaketModel> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        vm = ViewModelProvider(this)[HomeVM  ::class.java]
        isLoading()
        initRC()
        observe()

        recyclerView.adapter = dataAdapter
        val searchEditText = itemView.findViewById<EditText>(R.id.btnCari)
        dataAdapter.filter("")
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                dataAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun initRC(){
        binding.rcData.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            dataAdapter = UjianAdapter(
                dataList,
                requireContext(),
                { data -> editData(data) },
                { data -> hapusData(data) },
                { data -> cardClick(data) }
            )
        }
    }
    private fun observe(){
        vm.uid.observe(viewLifecycleOwner){
            if(it!==""){
                vm.getData(mFirestore,requireContext())
            }
        }
        vm.data.observe(viewLifecycleOwner){
            binding.include.shimmerContainer.startShimmer() // Start shimmer effect
            if(it.size>0){
                dataList.clear()
                dataAdapter.filteredDataList.clear()
                dataList.addAll(it)
                dataAdapter.filteredDataList.addAll(it)
                dataAdapter.notifyDataSetChanged()
                binding.include.shimmerContainer.stopShimmer() // Stop shimmer effect
                binding.include.shimmerContainer.visibility = View.GONE // Hide shimmer container
            }
        }
        vm.getUid(auth)
    }

    private fun editData(data: PaketModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(requireContext(), EditActivity::class.java)
        intent.putExtra("documentId", data.documentId)
        intent.putExtra("uid", data.uid)
        intent.putExtra("userId", data.userId)
        intent.putExtra("mapel", data.mapel)
        intent.putExtra("kelas", data.kelas)
        intent.putExtra("namaUjian", data.namaUjian)
        intent.putExtra("url", data.url)
        intent.putExtra("shortUrl", data.shortUrl)
        intent.putExtra("key", data.key)
        intent.putExtra("durasi", data.durasi)
        startActivity(intent)
    }
    private fun hapusData(data: PaketModel) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi")
        builder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
        builder.setPositiveButton("Ya") { dialogInterface: DialogInterface, i: Int ->
            vm.hapusDone.observe(viewLifecycleOwner){
                if(it){
                    dataList.remove(data)
                    dataAdapter.notifyDataSetChanged()
                    showSnackbarContext(requireContext(),"Berhasil dihapus")
                }
            }
            vm.doHapus(mFirestore,requireContext(),data.documentId.toString())
        }
        builder.setNegativeButton("Tidak") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()


    }
    private fun cardClick(data: PaketModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(requireContext(), UjianActivity::class.java)
        intent.putExtra("documentId", data.documentId)
        intent.putExtra("uid", data.uid)
        intent.putExtra("userId", data.userId)
        intent.putExtra("mapel", data.mapel)
        intent.putExtra("kelas", data.kelas)
        intent.putExtra("namaUjian", data.namaUjian)
        intent.putExtra("url", data.url)
        intent.putExtra("shortUrl", data.shortUrl)
        intent.putExtra("key", data.key)
        intent.putExtra("durasi", data.durasi)
        startActivity(intent)
    }
    fun isLoading(){
        vm.isLoading.observe(viewLifecycleOwner) {
            if (it){

                binding.ccLoading.contentLoading.visibility=View.VISIBLE
            }else{
                binding.ccLoading.contentLoading.visibility = View.GONE
            }
        }
    }
}