package com.vr.superexambropro.activity.guru

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.helper.showSnackBar

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etNama: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        rootView = findViewById<View>(android.R.id.content)
        initFirebase()
        initView()
        initClick()

    }
    private fun initFirebase(){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }
    private fun initView(){
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        progressBar = findViewById(R.id.progressBar)
    }
    private fun initClick(){
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val name = etNama.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() ) {
                progressBar.visibility = View.VISIBLE
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
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
                                    progressBar.visibility = View.GONE
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                showSnackBar(rootView,"Registration successful. Verification email sent. Please verify your email.")
                                            } else {
                                                showSnackBar(rootView,"Registration successful. Failed to send verification email.")
                                            }
                                        }
                                    startActivity(
                                        Intent(
                                            this,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE
                                    showSnackBar(rootView,"Failed to register user.")
                                }

                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            user?.updateProfile(profileUpdates)
                        } else {
                            progressBar.visibility = View.GONE
                            showSnackBar(rootView,"Registration failed. Please try again.")
                        }
                    }
            }else {
                showSnackBar(rootView,"Please fill in all required fields and pick an address.")
            }
        }
    }
}