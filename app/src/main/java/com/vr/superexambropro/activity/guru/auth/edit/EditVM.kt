package com.vr.superexambropro.activity.guru.auth.edit

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
import com.vr.superexambropro.helper.saveUser
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.PaketModel
import com.vr.superexambropro.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class EditVM(application: Application): AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : MutableLiveData<Boolean> = _isLoading
    private val _updateDone = MutableLiveData<Boolean>()
    val updateDone : MutableLiveData<Boolean> = _updateDone


    fun doUpdate(firestore: FirebaseFirestore,  context: Context,paketModel: PaketModel){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            val productData = hashMapOf(
                "namaUjian" to paketModel.namaUjian,
                "mapel" to paketModel.mapel,
                "kelas" to paketModel.kelas,
                "url" to paketModel.url,
                "durasi" to paketModel.durasi,
            )
            firestore.collection("paket")
                .document(paketModel.documentId.toString())
                .update(productData as Map<String, Any>)
                .addOnSuccessListener {
                    // Product updated successfully
                    _isLoading.postValue(false)
                    _updateDone.postValue(true)
                    showSnackbarContext(context, "Berhasil mengubah informasi")
                }
                .addOnFailureListener { e ->
                    // Error occurred while updating product
                    _isLoading.postValue(false)
                    _updateDone.postValue(false)
                    showSnackbarContext(context, "Gagal mengubah informasi")
                }
        }
    }
}