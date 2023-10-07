package com.vr.superexambropro.helper

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.activity.siswa.SelesaiActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun showSnackBar(view: View, message: String){
    //val rootView = findViewById<View>(android.R.id.content)
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}
fun generateRandomString(length: Int): String {
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}
fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDate = Date()
    return dateFormat.format(currentDate)
}
fun addMinutesToCurrentDate(minutesToAdd: Int): String {
    val calendar = Calendar.getInstance()
    calendar.time = Date()
    calendar.add(Calendar.MINUTE, minutesToAdd)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(calendar.time)
}
fun formatDateToIndonesian(date: Date): String {
    val format = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return format.format(date)
}
fun convertStringToDate(dateString: String, formatPattern: String): Date? {
    try {
        val dateFormat = SimpleDateFormat(formatPattern, Locale("id", "ID"))
        return dateFormat.parse(dateString)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

 var countDownTimer: CountDownTimer? = null
 var timerRunning = false
 var timeLeftInMillis: Long = 0
fun startTimer(view:View,waktudalammenit: Int, textView: TextView, documentId: String,
               context: Context,lockStatus:Boolean, activity: AppCompatActivity) {
    val totalMillis = waktudalammenit * 60000 // Konversi menit ke milidetik (1 menit = 60000 ms)
    countDownTimer?.cancel()
    countDownTimer = object : CountDownTimer(totalMillis.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            timeLeftInMillis = millisUntilFinished
            updateCountdownText(textView)
            if (millisUntilFinished.toInt() == 60000) {
                showSnackBar(view,"Hanya tersisa satu menit lagi. Pastikan kamu sudah menekan tombol selesai di soal agar jawaban tersimpan.")
            }
        }
        override fun onFinish() {
            timerRunning = false
            // Timer selesai
            updateFirebase("DetailActivity", FirebaseFirestore.getInstance(), "ujian",
                documentId, hashMapOf("status" to "Selesai"))
            { unlockLockScreen(lockStatus, context, activity) }
        }
    }
    countDownTimer?.start()
    timerRunning = true
}
fun startTimerSiswa(waktudalammenit: Int, textView: TextView) {
    val totalMillis = waktudalammenit * 60000 // Konversi menit ke milidetik (1 menit = 60000 ms)
    countDownTimer?.cancel()
    countDownTimer = object : CountDownTimer(totalMillis.toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            timeLeftInMillis = millisUntilFinished
            updateCountdownText(textView)
        }
        override fun onFinish() {
            timerRunning = false
            // Timer selesai
            textView.text = "Selesai"
        }
    }
    countDownTimer?.start()
    timerRunning = true
}
fun calculateRemainingTime(workDate: String, durationInMinutes: Long): Long {
    // Format tanggal pengerjaan
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    try {
        // Parse tanggal pengerjaan ke dalam objek Date
        val workDateTime = dateFormat.parse(workDate)

        // Hitung selisih waktu antara tanggal pengerjaan dan waktu sekarang dalam milidetik
        val currentTimeMillis = System.currentTimeMillis()
        val workTimeMillis = workDateTime?.time ?: 0
        var timeDifferenceMillis = currentTimeMillis - workTimeMillis

        // Konversi durasi dalam menit ke milidetik
        val durationMillis = durationInMinutes * 60 * 1000

        // Jika selisih waktu kurang dari 0, set ke 0
        if (timeDifferenceMillis < 0) {
            timeDifferenceMillis = 0
        }

        // Mengembalikan selisih waktu atau 0 jika durasi tidak bersisa
        return if (timeDifferenceMillis < durationMillis) {
            durationMillis - timeDifferenceMillis
        } else {
            0
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Jika terjadi kesalahan, kembalikan 0
    return 0
}
 fun updateCountdownText(textView: TextView) {
    val hours = (timeLeftInMillis / 3600000).toInt()
    val minutes = ((timeLeftInMillis % 3600000) / 60000).toInt()
    val seconds = ((timeLeftInMillis % 60000) / 1000).toInt()

    val timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    textView.text = timeLeftFormatted
}

fun updateFirebase(TAG:String, db: FirebaseFirestore, collection: String, document: String, data: HashMap<String, Any>
                   , onComplete: () -> Unit){
    db.collection(collection).document(document)
        .update(data)
        .addOnSuccessListener {
            onComplete()
            Log.d(TAG, "DocumentSnapshot successfully updated!")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error updating document", e)
        }
}

fun unlockLockScreen(test_lock:Boolean,context: Context, activity: AppCompatActivity){
    if (test_lock) {
        val prefs = context.getSharedPreferences("rate_dialog", AppCompatActivity.MODE_PRIVATE)
        val rated = prefs.getBoolean("rate", false)
        //intent ke selesaiActivity
        val intent = Intent(context, SelesaiActivity::class.java)
        //startActivity(intent)
        activity.startActivity(intent)
        activity.finish()
    } else {
        val intent = Intent(context, SelesaiActivity::class.java)
        //startActivity(intent)
        activity.startActivity(intent)
        activity.finish()
    }
}

