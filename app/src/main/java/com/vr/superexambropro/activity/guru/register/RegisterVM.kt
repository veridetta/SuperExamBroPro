package com.vr.superexambropro.activity.guru.register

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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.activity.guru.login.LoginActivity
import com.vr.superexambropro.helper.saveUser
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext
import com.vr.superexambropro.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RegisterVM(application: Application): AndroidViewModel(application) {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : MutableLiveData<Boolean> = _isLoading
    private val _hasRegister = MutableLiveData<Boolean>()
    val hasRegister : MutableLiveData<Boolean> = _hasRegister
    private val _role = MutableLiveData<String>()
    val role : MutableLiveData<String> = _role
    private val _hasFireStore = MutableLiveData<Boolean>()
    val hasFireStore : MutableLiveData<Boolean> = _hasFireStore

    fun register(auth:FirebaseAuth,firestore: FirebaseFirestore,  context: Context, activity: RegisterActivity,
              email:String, password:String,name:String){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        _hasRegister.postValue(true)
                    } else {
                        _hasRegister.postValue(false)
                        showSnackbarContext(context,"Registration failed. Please try again.")
                    }
                }
        }
    }
    fun fireStore(auth:FirebaseAuth,firestore: FirebaseFirestore,context: Context,name:String,email:String){
        _isLoading.postValue(true)
        CoroutineScope(Dispatchers.IO).run {
            val user = auth.currentUser
            val uid = user?.uid ?: ""
            val newUser = hashMapOf(
                "uid" to uid,
                "email" to email,
                "name" to name,
                "provider" to "email",
                "role" to "guru",
                "isVerified" to false,
                "created_at" to ""
            )
            firestore.collection("users").document(uid)
                .set(newUser)
                .addOnSuccessListener {
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            _isLoading.postValue(false)
                            if (verificationTask.isSuccessful) {
                                _hasFireStore.postValue(true)
                                showSnackbarContext(context,"Registration successful. Verification email sent. Please verify your email.")
                            } else {
                                _hasFireStore.postValue(false)
                                showSnackbarContext(context,"Registration successful. Failed to send verification email.")
                            }
                        }
                }
                .addOnFailureListener {
                    _isLoading.postValue(false)
                    showSnackbarContext(context,"Failed to register user.")
                }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user?.updateProfile(profileUpdates)
        }
    }
}