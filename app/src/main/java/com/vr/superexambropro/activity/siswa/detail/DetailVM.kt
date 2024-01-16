package com.vr.superexambropro.activity.siswa.detail

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.helper.addMinutesToCurrentDate
import com.vr.superexambropro.helper.generateRandomString
import com.vr.superexambropro.helper.getCurrentDate
import com.vr.superexambropro.helper.saveUser
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.PaketModel
import com.vr.superexambropro.model.UjianModel
import com.vr.superexambropro.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class DetailVM(application: Application): AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : MutableLiveData<Boolean> = _isLoading
    private val _data = MutableLiveData<MutableList<PaketModel>>()
    val data : MutableLiveData<MutableList<PaketModel>> = _data
    private val _docId = MutableLiveData<String>()
    val docId : MutableLiveData<String> = _docId

    fun getData(firestore: FirebaseFirestore,  context: Context,shortUrl:String){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            firestore.collection("paket").whereEqualTo("shortUrl",shortUrl).get().addOnSuccessListener {
                val listData = mutableListOf<PaketModel>()
                for (document in it.documents){
                    val data = document.toObject(PaketModel::class.java)
                    data?.let { it1 -> listData.add(it1) }
                }
                _data.postValue(listData)
                _isLoading.postValue(false)
            }.addOnFailureListener {
                _isLoading.postValue(false)
                showSnackbarContext(context,"Gagal memuat data")
            }
        }
    }

    fun doInsert(firestore: FirebaseFirestore,  context: Context,
                 data: UjianModel){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            val ujianData = hashMapOf(
                "uid" to UUID.randomUUID().toString(),
                "paketId" to data.paketId,
                "namaSiswa" to data.namaSiswa,
                "waktuMulai" to data.waktuMulai,
                "waktuSelesai" to data.waktuSelesai,
                "status" to data.status,
                "kodeKeamanan" to data.kodeKeamanan,
                "durasi" to data.durasi,
                "created_at" to data.created_at,
            )
            firestore.collection("ujian")
                .add(ujianData as Map<String, Any>)
                .addOnSuccessListener { documentReference ->
                    _docId.postValue(documentReference.id)
                    _isLoading.postValue(false)
                }.addOnFailureListener {
                    _isLoading.postValue(false)
                    showSnackbarContext(context,"Gagal menyimpan data")
                }
        }
    }
}