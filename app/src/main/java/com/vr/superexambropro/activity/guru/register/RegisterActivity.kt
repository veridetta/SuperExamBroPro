package com.vr.superexambropro.activity.guru.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.guru.login.LoginActivity
import com.vr.superexambropro.activity.guru.login.LoginVM
import com.vr.superexambropro.databinding.ActivityLoginBinding
import com.vr.superexambropro.databinding.ActivityRegisterBinding
import com.vr.superexambropro.helper.showSnackBar

class RegisterActivity : AppCompatActivity() {
    private lateinit var vm: RegisterVM
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFirebase()
        initActivity()
        isLoading()
        initClick()

    }
    private fun initFirebase(){
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }
    private fun initActivity(){
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm = ViewModelProvider(this)[RegisterVM::class.java]
    }

    private fun initClick(){
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etNama.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() ) {
                vm.hasRegister.observe(this){
                    if(it){
                        vm.fireStore(auth,firestore,this,name,email)
                    }
                }
                vm.hasFireStore.observe(this){
                    if(it){
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }
                }
                vm.register(auth,firestore,this,this,email,password,name)
            }else {
                showSnackBar(rootView,"Please fill in all required fields and pick an address.")
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
}