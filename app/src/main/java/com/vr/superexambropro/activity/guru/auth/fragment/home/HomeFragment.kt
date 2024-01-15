package com.vr.superexambropro.activity.guru.auth.fragment.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.EditActivity
import com.vr.superexambropro.activity.guru.auth.UjianActivity
import com.vr.superexambropro.activity.guru.login.LoginVM
import com.vr.superexambropro.adapter.UjianAdapter
import com.vr.superexambropro.databinding.ActivityLoginBinding
import com.vr.superexambropro.databinding.FragmentHomeBinding
import com.vr.superexambropro.model.PaketModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private lateinit var vm: HomeVM
    private lateinit var binding: FragmentHomeBinding

    private val mFirestore = FirebaseFirestore.getInstance()
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
        recyclerView = itemView.findViewById(R.id.rcData)
        recyclerView.apply {
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
        val shimmerContainer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(mFirestore,shimmerContainer, it) }

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

    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout, uid: String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("paket")
                    .whereEqualTo("userId", uid)
                    .orderBy("created_at", Query.Direction.DESCENDING) // Menggunakan orderBy dengan descending order
                    .get()
                    .await()

                val datas = mutableListOf<PaketModel>()
                for (document in result) {
                    val data = document.toObject(PaketModel::class.java)
                    val docId = document.id
                    data.documentId = docId
                    datas.add(data)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    // Menghapus data yang ada sebelumnya
                    dataList.clear()
                    dataAdapter.filteredDataList.clear()

                    dataList.addAll(datas)
                    dataAdapter.filteredDataList.addAll(datas)
                    dataAdapter.notifyDataSetChanged()
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                withContext(Dispatchers.Main) {
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            }
        }
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
            // Jika pengguna memilih "Ya", keluar dari aplikasi
            //hapus data dari firestore
            mFirestore.collection("paket").document(data.documentId!!).delete()
                .addOnSuccessListener {
                    Log.d("Hapus", "Data successfully deleted!")
                }
                .addOnFailureListener { e -> Log.w("Hapus", "Error deleting document", e) }
            //remove data dari adapter
            dataList.remove(data)
            dataAdapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("Tidak") { dialogInterface: DialogInterface, i: Int ->
            // Jika pengguna memilih "Tidak", tutup dialog
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

}