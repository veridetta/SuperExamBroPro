package com.vr.superexambropro.activity.guru.auth.fragment.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.helper.saveUser
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.PaketModel
import com.vr.superexambropro.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await

class HomeVM(application: Application): AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : MutableLiveData<Boolean> = _isLoading
    private val _uid = MutableLiveData<String>()
    val uid : MutableLiveData<String> = _uid
    private val _data = MutableLiveData<MutableList<PaketModel>>()
    val data : MutableLiveData<MutableList<PaketModel>> = _data
    private val _hapusDone = MutableLiveData<Boolean>()
    val hapusDone : MutableLiveData<Boolean> = _hapusDone

    fun getUid(auth: FirebaseAuth){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            val uidx = auth.currentUser?.uid
            if(uidx!==""){
                _isLoading.postValue(false)
                _uid.postValue(uidx.toString())
            }else{
                _isLoading.postValue(false)
                _uid.postValue("")
            }
        }
    }
    fun getData(firestore: FirebaseFirestore,  context: Context){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            firestore.collection("paket")
                .whereEqualTo("userId", uid)
                .orderBy("created_at", Query.Direction.DESCENDING) // Menggunakan orderBy dengan descending order
                .addSnapshotListener { snapshots, exception ->
                    if (exception != null) {
                        _isLoading.postValue(false)
                        showSnackbarContext(context, exception.message.toString())
                        return@addSnapshotListener
                    }
                    val datas = mutableListOf<PaketModel>()
                    for (document in snapshots!!) {
                        val data = document.toObject(PaketModel::class.java)
                        val docId = document.id
                        data.documentId = docId
                        datas.add(data)
                    }
                    _isLoading.postValue(false)
                    _data.postValue(datas)
                }

        }
    }
    fun doHapus(firestore: FirebaseFirestore,context: Context,documentId:String){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            firestore.collection("paket").document(documentId).delete()
                .addOnSuccessListener {
                    Log.d("Hapus", "Data successfully deleted!")
                    _isLoading.postValue(false)
                    _hapusDone.postValue(true)
                }
                .addOnFailureListener { e -> Log.w("Hapus", "Error deleting document", e) }
        }
    }
}