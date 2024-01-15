package com.vr.superexambropro.helper

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.vr.superexambropro.model.UserModel

fun saveUser(user:UserModel, context: Context){
    val sharedPreferences = context.getSharedPreferences("MyPrefs",
        AppCompatActivity.MODE_PRIVATE
    )
    val editor = sharedPreferences.edit()
    editor.putBoolean("isLogin", true)
    editor.putString("userRole", user.role)
    editor.putString("userUid", user.uid)
    editor.putString("userName", user.name)
    editor.putString("userEmail", user.email)
    editor.apply()
}