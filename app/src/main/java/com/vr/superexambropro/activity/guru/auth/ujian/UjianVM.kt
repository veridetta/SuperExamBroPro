package com.vr.superexambropro.activity.guru.auth.ujian

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
import com.vr.superexambropro.model.UjianModel
import com.vr.superexambropro.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class UjianVM(application: Application): AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : MutableLiveData<Boolean> = _isLoading
    private val _data = MutableLiveData<MutableList<UjianModel>>()
    val data : MutableLiveData<MutableList<UjianModel>> = _data


    fun getData(firestore: FirebaseFirestore,  context: Context,paketId:String){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            firestore.collection("ujian").whereEqualTo("paketId",paketId)
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        _isLoading.postValue(false)
                        showSnackbarContext(context, exception.message.toString())
                        return@addSnapshotListener
                    }
                    val datas = mutableListOf<UjianModel>()

                    for (document in snapshots?.documents.orEmpty()) {
                        val data = document.toObject(UjianModel::class.java)
                        val docId = document.id
                        data?.let {
                            it.documentId = docId
                            datas.add(it)
                        }
                    }

                    _data.postValue(datas)
                    _isLoading.postValue(false)
                }
        }
    }
}