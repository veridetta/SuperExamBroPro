package com.vr.superexambropro.activity.guru.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.RoleActivity
import com.vr.superexambropro.activity.guru.auth.fragment.AddFragment
import com.vr.superexambropro.activity.guru.auth.fragment.home.HomeFragment

class GuruActivity : AppCompatActivity() {
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guru)
        initView()
        initFragment()
    }

    private fun initView(){
        fragmentContainer = findViewById(R.id.fragmentContainer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }
    private fun initFragment(){
        val homeFragment = HomeFragment()
        //dapatkan intent dari activity sebelumnya
        val intent = intent
        //dapatkan data dari intent
        val fragment = intent.getStringExtra("fragment")
        if (fragment != null) {
            if (fragment == "home") {
                val orderFragment = HomeFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_home
            }else if (fragment == "add") {
                val orderFragment = AddFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_add
            }else if (fragment == "logout") {
                val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                startActivity(Intent(this,RoleActivity::class.java))
                finish()
            }
        }else{
            replaceFragment(homeFragment)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    val homeFragment = HomeFragment()
                    replaceFragment(homeFragment)
                    true
                }
                R.id.menu_add -> {
                    val orderFragment = AddFragment()
                    replaceFragment(orderFragment)
                    true
                }

                R.id.menu_logout -> {
                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                    startActivity(Intent(this,RoleActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}