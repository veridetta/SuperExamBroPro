package com.vr.superexambropro.activity.guru.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.register.RegisterActivity
import com.vr.superexambropro.activity.guru.auth.GuruActivity
import com.vr.superexambropro.databinding.ActivityLoginBinding
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.showSnackbarContext

class LoginActivity : AppCompatActivity() {
    //new
    private lateinit var vm: LoginVM
    private lateinit var binding: ActivityLoginBinding
    //end new
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var contentView: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivity()
        isLoading()
        initFirebase()
        initClick()
    }

    private fun initActivity(){
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[LoginVM::class.java]
    }

    private fun initFirebase(){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }
    private fun initClick(){
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            vm.role.observe(this) {
                if (it!==""){
                    when (it) {
                        "guru" -> startActivity(
                            Intent(
                                this,
                                GuruActivity::class.java
                            )
                        )
                    }
                }
            }
            vm.login(auth,firestore,this,this,email,password)
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.textViewForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isNotEmpty()) {
                vm.hasReset.observe(this){
                    if(it){
                        showSnackbarContext(this,"Email reset telah dikirim ulang.")
                    }
                }
                vm.forgot(auth,this,email)
            } else {
                showSnackBar(contentView,"Masukkan alamat email terlebih dahulu.")
            }
        }

        binding.btnGoogle.setOnClickListener {
            // Implement Google login
            // Handle login success and redirection
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
            //ambil data dari firestore users berdasarkan uid
            //cek dlu apakah user berhasil login dengan google atau tidak
            if(auth.currentUser != null){
                val user = auth.currentUser
                vm.role.observe(this) {
                    if (it!==""){
                        when (it) {
                            "guru" -> startActivity(
                                Intent(
                                    this,
                                    GuruActivity::class.java
                                )
                            )
                        }
                    }
                }
                vm.google(firestore,this,user!!)
            }
        }
    }
    fun isLoading(){
        vm.isLoading.observe(this) {
            if (it){

                binding.ccLoading.contentLoading.visibility=View.VISIBLE
            }else{
                binding.ccLoading.contentLoading.visibility = View.GONE
            }
        }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}