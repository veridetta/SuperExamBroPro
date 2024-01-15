package com.vr.superexambropro.activity.guru.login

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
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.helper.saveUser
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class LoginVM(application: Application): AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : MutableLiveData<Boolean> = _isLoading
    private val _hasLogin = MutableLiveData<Boolean>()
    val hasLogin : MutableLiveData<Boolean> = _hasLogin
    private val _role = MutableLiveData<String>()
    val role : MutableLiveData<String> = _role
    private val _hasReset = MutableLiveData<Boolean>()
    val hasReset : MutableLiveData<Boolean> = _hasReset

    fun login(auth:FirebaseAuth,firestore: FirebaseFirestore,  context: Context, activity: LoginActivity,
              email:String, password:String){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("verified: ","email is verified")
                        firestore.collection("users").document(user!!.uid)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                val userRole = documentSnapshot.getString("role")
                                var user = UserModel()
                                user.role= userRole
                                user.isLogin= true
                                user.uid=documentSnapshot.getString("uid")
                                user.name=documentSnapshot.getString("name")
                                user.email=documentSnapshot.getString("email")
                                saveUser(user,context)
                                _role.postValue(userRole.toString())
                                _isLoading.postValue(false)
                            }
                            .addOnFailureListener {
                                _role.postValue("")
                                _isLoading.postValue(false)
                                showSnackbarContext(context,"Failed to get user role.")
                            }

                    } else {
                        _role.postValue("")
                        _isLoading.postValue(false)
                        showSnackbarContext(context,"Login failed. Please check your credentials.")
                    }
                }
        }
    }
    fun forgot(auth:FirebaseAuth,context: Context,email:String){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _hasReset.postValue(true)
                    } else {
                        _hasReset.postValue(false)
                        showSnackbarContext(context,"Gagal mengirim email reset ulang. Periksa alamat email.")
                    }
                    _isLoading.postValue(false)
                }
        }
    }
    fun google(firestore: FirebaseFirestore,context: Context,user: FirebaseUser){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            firestore.collection("users").document(user!!.uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val userRole = documentSnapshot.getString("role")
                    var user = UserModel()
                    user.role= userRole
                    user.isLogin= true
                    user.uid=documentSnapshot.getString("uid")
                    user.name=documentSnapshot.getString("name")
                    user.email=documentSnapshot.getString("email")
                    saveUser(user,context)
                    _role.postValue(userRole.toString())
                    _isLoading.postValue(false)
                }
                .addOnFailureListener {
                    _isLoading.postValue(false)
                    showSnackbarContext(context,"Failed to get user role.")
                }
        }
    }
}