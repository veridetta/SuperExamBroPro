package com.vr.superexambropro.activity.guru.auth.fragment.add

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.helper.generateRandomString
import com.vr.superexambropro.helper.getCurrentDate
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.PaketModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.UUID

class AddVM(application: Application): AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : MutableLiveData<Boolean> = _isLoading
    private val _uid = MutableLiveData<String>()
    val uid : MutableLiveData<String> = _uid
    private val _insertDone = MutableLiveData<Boolean>()
    val insertDone : MutableLiveData<Boolean> = _insertDone
    fun doInsert(firestore: FirebaseFirestore,auth: FirebaseAuth,
                 context: Context,data:PaketModel){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            val sharedPreferences = context.getSharedPreferences("MyPrefs",
                Context.MODE_PRIVATE
            )
            val userName = sharedPreferences.getString("userName", "")
            val currentUser = auth.currentUser
            val barangData = hashMapOf(
                "uid" to UUID.randomUUID().toString(),
                "userId" to currentUser!!.uid,
                "namaUjian" to data.namaUjian,
                "mapel" to data.mapel,
                "kelas" to data.kelas,
                "namaGuru" to userName,
                "url" to data.url,
                "shortUrl" to generateRandomString(8),
                "key" to generateRandomString(8),
                "durasi" to data.durasi,
                "status" to "Belum",
                "created_at" to getCurrentDate(),
            )
            firestore.collection("paket")
                .add(barangData as Map<String, Any>)
                .addOnSuccessListener { documentReference ->
                    _isLoading.postValue(false)
                    showSnackbarContext(context,"Berhasil menyimpan data")
                    _insertDone.postValue(true)
                }
                .addOnFailureListener { e ->
                    // Error occurred while adding product
                    _isLoading.postValue(false)
                    showSnackbarContext(context,"Gagal menyimpan data ${e.message}")
                }
        }
    }
}