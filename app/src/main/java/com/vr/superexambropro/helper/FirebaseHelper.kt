package com.vr.superexambropro.helper

import android.view.View
import com.facebook.shimmer.ShimmerFrameLayout
import com.flurry.sdk.it
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.model.PaketModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Interface untuk callback generik
interface DataCallback<T> {
    fun onDataLoaded(datas: List<T>)
    fun onError(message: String)
}

// Fungsi readData dengan tipe data generik
fun <T> readDataFirebase(
    db: FirebaseFirestore,
    shimmerContainer: ShimmerFrameLayout,
    collection: String,
    filter: String,
    key : String,
    value : String,
    dataType: Class<T>, // Tipe data yang sesuai dengan callback
    callback: DataCallback<T>
) {
    shimmerContainer.startShimmer() // Start shimmer effect
    GlobalScope.launch(Dispatchers.IO) {
        try {
            if(filter == ""){
                val result = db.collection(collection).get().await()
                val datas = mutableListOf<T>()
                for (document in result.documents) {
                    val data = document.toObject(dataType)
                    val docId = document.id
                    for (document in result.documents) {
                        val data = document.toObject(dataType)
                        val docId = document.id
                        data?.let {
                            // Masukkan docId ke dalam properti 'id' dalam objek data
                            datas.add(it)
                        }
                    }

                }

                withContext(Dispatchers.Main) {
                    callback.onDataLoaded(datas) // Panggil callback dengan data yang dimuat
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            }else if (filter == "doc"){
                val result = db.collection(collection).document(value).get().await()
                val datas = mutableListOf<T>()
                val data = result.toObject(dataType)
                data?.let {
                    datas.add(it)
                }
                withContext(Dispatchers.Main) {
                    callback.onDataLoaded(datas) // Panggil callback dengan data yang dimuat
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            }else if(filter == "where"){
                val result = db.collection(collection).whereEqualTo(key, value).get().await()
                val datas = mutableListOf<T>()
                for (document in result.documents) {
                    val data = document.toObject(dataType)
                    data?.let {
                        addDocumentIdToData(it, document.id)
                        datas.add(it)
                    }
                }

                withContext(Dispatchers.Main) {
                    callback.onDataLoaded(datas) // Panggil callback dengan data yang dimuat
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            }
        } catch (e: Exception) {
            val errorMessage = "Error getting documents : $e"
            withContext(Dispatchers.Main) {
                callback.onError(errorMessage) // Panggil callback dengan pesan kesalahan
                shimmerContainer.stopShimmer() // Stop shimmer effect
                shimmerContainer.visibility = View.GONE // Hide shimmer container
            }
        }
    }
}
fun addDocumentIdToData(data: Any, docId: String) {
    try {
        // Gunakan refleksi untuk menambahkan properti 'documentId' ke dalam objek data
        val field = data.javaClass.getDeclaredField("documentId")
        field.isAccessible = true
        field.set(data, docId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}