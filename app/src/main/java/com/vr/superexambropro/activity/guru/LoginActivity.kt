package com.vr.superexambropro.activity.guru

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.helper.showSnackBar

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    // Declare UI elements
    private lateinit var buttonLogin: Button
    private lateinit var contentView: RelativeLayout
    private lateinit var btnRegister: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewForgotPassword: TextView
    private lateinit var buttonGoogle: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initFirebase()
        initView()
        initClick()
    }

    private fun initFirebase(){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }
    private fun initView(){
        buttonLogin = findViewById(R.id.btnLogin)
        contentView = findViewById(R.id.contentView)
        btnRegister = findViewById(R.id.btnRegister)
        editTextEmail = findViewById(R.id.etEmail)
        editTextPassword = findViewById(R.id.etPassword)
        progressBar = findViewById(R.id.progressBar)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
        buttonGoogle = findViewById(R.id.btnGoogle)
    }
    private fun initClick(){
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            progressBar.visibility = View.VISIBLE

            // Authenticate using Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("verified: ","email is verified")
                        firestore.collection("users").document(user!!.uid)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                val userRole = documentSnapshot.getString("role")

                                // Save user role to SharedPreferences
                                val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isLogin", true)
                                editor.putString("userRole", userRole)
                                editor.putString("userUid", documentSnapshot.getString("uid"))
                                editor.putString("userName", documentSnapshot.getString("name"))
                                editor.putString("userEmail", documentSnapshot.getString("email"))
                                editor.apply()

                                // Redirect to appropriate activity based on user role
                                when (userRole) {
                                    "guru" -> startActivity(
                                        Intent(
                                            this,
                                            GuruActivity::class.java
                                        )
                                    )
                                }
                                finish()
                            }
                            .addOnFailureListener {
                                progressBar.visibility = View.GONE
                                showSnackBar(contentView,"Failed to get user role.")
                            }

                    } else {
                        progressBar.visibility = View.GONE
                        showSnackBar(contentView,"Login failed. Please check your credentials.")
                    }
                }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        textViewForgotPassword.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (email.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE

                // Kirim ulang email verifikasi ke alamat email pengguna
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            showSnackBar(contentView,"Email reset telah dikirim ulang.")
                        } else {
                            showSnackBar(contentView,"Gagal mengirim email reset ulang. Periksa alamat email.")
                        }
                    }
            } else {
                showSnackBar(contentView,"Masukkan alamat email terlebih dahulu.")
            }
        }

        buttonGoogle.setOnClickListener {
            // Implement Google login
            // Handle login success and redirection
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}