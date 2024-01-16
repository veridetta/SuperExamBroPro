package com.vr.superexambropro.activity.guru.auth.fragment.add

import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.activity.guru.auth.fragment.home.HomeVM
import com.vr.superexambropro.databinding.FragmentAddBinding
import com.vr.superexambropro.databinding.FragmentHomeBinding
import com.vr.superexambropro.helper.generateRandomString
import com.vr.superexambropro.helper.getCurrentDate
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.model.PaketModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddFragment : Fragment() {
    private lateinit var vm: AddVM
    private lateinit var binding: FragmentAddBinding

    private  var auth= FirebaseAuth.getInstance()
    private  var firestore= FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        vm = ViewModelProvider(this)[AddVM  ::class.java]
        isLoading()
        initClick()
    }
    private fun initClick(){
        binding.btnAdd.setOnClickListener {
            val namaUjian = binding.etNamaUjian.text.toString()
            val mapel = binding.etMapel.text.toString()
            val kelas = binding.etKelas.text.toString()
            val durasi = binding.etDurasi.text.toString()
            val url = binding.etUrl.text.toString()
            if (namaUjian.isNotEmpty() && mapel.isNotEmpty() && kelas.isNotEmpty() && durasi.isNotEmpty()
                && url.isNotEmpty()) {
                addDataToFirestore(
                    namaUjian,
                    mapel,
                    kelas,
                    durasi,
                    url
                )
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
        val data = PaketModel()
        data.namaUjian = namaUjian
        data.mapel = mapel
        data.kelas = kelas
        data.durasi = durasi
        data.url = url

        vm.doInsert(firestore,auth,requireContext(),data)
        vm.insertDone.observe(viewLifecycleOwner) {
            if (it){
                val intent = Intent(requireContext(), GuruActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
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