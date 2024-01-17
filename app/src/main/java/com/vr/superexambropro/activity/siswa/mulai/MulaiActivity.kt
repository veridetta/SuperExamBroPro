package com.vr.superexambropro.activity.siswa.mulai

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.helper.generateRandomString
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.startTimer
import com.vr.superexambropro.helper.unlockLockScreen
import com.vr.superexambropro.helper.updateFirebase
import im.delight.android.webview.AdvancedWebView
import java.util.Timer
import java.util.TimerTask


class MulaiActivity() : AppCompatActivity(),AdvancedWebView.Listener  {
    var firstTime = true
    lateinit var btnRefresh :ImageButton
    lateinit var btnSelesai : LinearLayout
    lateinit var btnLanjut : Button
    lateinit var tvTimer  : TextView
    lateinit var contentView  : RelativeLayout
    private lateinit var webView: AdvancedWebView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnYakin: Button
    private lateinit var btnKembali: Button
    private lateinit var lyKonfirm: RelativeLayout
    private lateinit var lyNotif: RelativeLayout
    private lateinit var etKode: EditText
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var wakelockAcquired = false
    var durasi = ""
    var isSelesai  = false
    var idUjian = ""
    var url =""
    var kode = ""
    private var powerButtonReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mengatur tipe jendela di sini sebelum konten tampilan ditentukan
        window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG)
        setContentView(R.layout.activity_mulai)
        initView()
        initPower()
        detectRecorder()
        initClick()
        initIntent()
        initWebview()
        initTimer()
        //handler untuk delay 3 detik ganti firttime ke false
        val handler = android.os.Handler()
        handler.postDelayed({
            firstTime = false
        }, 3000)
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE,
            "MulaiActivity:WakeLock"
        )
    }
    private fun initView(){
        btnRefresh = findViewById(R.id.btnRefresh)
        btnSelesai = findViewById(R.id.btnSelesai)
        tvTimer = findViewById(R.id.tvTimer)
        contentView = findViewById(R.id.contentView)
        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progressBar)
        btnYakin = findViewById(R.id.btnYakin)
        btnKembali = findViewById(R.id.btnKembali)
        btnLanjut = findViewById(R.id.btnlanjut)
        lyKonfirm = findViewById(R.id.lyKonfirm)
        lyNotif = findViewById(R.id.lyDialog)
        etKode = findViewById(R.id.etKode)
        lyKonfirm.visibility = View.GONE
        lyNotif.visibility = View.GONE
    }
    private fun initClick(){
        btnRefresh.setOnClickListener {
            btnRefresh.isEnabled = false
            progressBar.visibility = View.VISIBLE
            webView.reload()
        }
        btnSelesai.setOnClickListener {
            lyKonfirm.visibility = View.VISIBLE
        }
        btnKembali.setOnClickListener {
            lyKonfirm.visibility = View.GONE
        }
        btnYakin.setOnClickListener {
            isSelesai = true
            updateFirebase("DetailActivity", FirebaseFirestore.getInstance(), "ujian",
                idUjian, hashMapOf("status" to "Selesai"))
            {
                showSnackBar(contentView,"Berhasil menyimpan data")
                unlockLockScreen(true,this,this@MulaiActivity)
            }
        }
        btnLanjut.setOnClickListener {
            if (etKode.text.toString() == kode){
                var kodeBaru = generateRandomString(6)
                kode = kodeBaru
                updateFirebase("DetailActivity", FirebaseFirestore.getInstance(), "ujian",
                    idUjian, hashMapOf("status" to "Sedang Mengerjakan","kodeKeamanan" to kode))
                { showSnackBar(contentView,"Berhasil melanjutkan ujian") }
                lyNotif.visibility = View.GONE
            }else{
                showSnackBar(contentView,"Kode salah, silahkan coba lagi")
            }
        }
    }
    private fun initIntent(){
        durasi = intent.getStringExtra("durasi").toString()
        idUjian = intent.getStringExtra("documentId").toString()
        url = intent.getStringExtra("url").toString()
        kode = intent.getStringExtra("kode").toString()
    }
    private fun initTimer(){
        startTimer(contentView,durasi.toInt(), tvTimer, idUjian,
            this,false,this,btnYakin)
    }
    fun initWebview() {
        /// Konfigurasi WebView
        webView.setListener(this,this)
        webView.getSettings().setJavaScriptEnabled(true)
        webView.getSettings().setUseWideViewPort(false)
        webView.getSettings().setLoadWithOverviewMode(true)
        webView.getSettings().setSupportZoom(true)
        webView.getSettings().setBuiltInZoomControls(false)
        webView.getSettings().setDisplayZoomControls(false)
        //cache mode no cache
        webView.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.clearCache(true)
        webView.clearHistory()
        //check url ada https atau http tidak di depannya
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        loadWebPage(url)
        Log.d("TAG", "initWebview: $url")
    }
    fun initPower(){
        // Daftarkan penerima untuk mengawasi tombol daya
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        powerButtonReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Tindakan yang ingin Anda lakukan saat tombol daya ditekan
                showSnackBar(contentView,"Tombol power tidak bisa digunakan")
            }
        }
        registerReceiver(powerButtonReceiver, filter)
    }
    fun detectRecorder(){
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appTasks = am.appTasks
            for (task in appTasks) {
                if (task.taskInfo.baseActivity!!.packageName == packageName) {
                   showSnackBar(contentView,"Tidak bisa merekam layar")


                }
            }
        }

    }
    private fun loadWebPage(url: String) {
        webView.loadUrl(url)
    }

    private fun hideNavigationBar() {
        val decorView: View = this.window.decorView
        val uiOptions: Int = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread { decorView.systemUiVisibility = uiOptions }
            }
        }
        timer.scheduleAtFixedRate(task, 1, 2)
    }
    override fun onBackPressed() {
        if (!webView.onBackPressed()) {
            return
        }
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return false
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            hideNavigationBar()
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //this.window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_HOME) {
            // Mencegah akses tombol beranda
            true
        } else super.onKeyDown(keyCode, event)
    }
    override fun onDestroy() {
        webView.onDestroy()
        super.onDestroy()
        // Hapus penerima saat aktivitas dihancurkan
        if (powerButtonReceiver != null) {
            unregisterReceiver(powerButtonReceiver)
        }
        //normalkan wakelock

    }

    override fun onPause() {
        webView.onPause();
        super.onPause()
        // Aplikasi sedang dalam status pause (misalnya, ketika pengguna meminimalkan aplikasi)
        // Tambahkan kode yang harus dijalankan saat aplikasi di-pause di sini
        if(!isSelesai){
            if (!firstTime) {
                lyNotif.visibility = View.VISIBLE
                updateFirebase("DetailActivity", FirebaseFirestore.getInstance(), "ujian",
                    idUjian, hashMapOf("status" to "Keluar Aplikasi"))
                { showSnackBar(contentView,"Anda telah keluar dari aplikasi") }
            }
        }
    }
    //on destroy
    override fun onResume() {
        super.onResume()
        webView.onResume();
        // Aplikasi sedang dalam status resume (misalnya, ketika pengguna kembali ke aplikasi)
        // Tambahkan kode yang harus dijalankan saat aplikasi di-resume di sini
        if(!isSelesai){
            if (!firstTime) {
                lyNotif.visibility = View.VISIBLE
                updateFirebase("DetailActivity", FirebaseFirestore.getInstance(), "ujian",
                    idUjian, hashMapOf("status" to "Keluar Aplikasi"))
                { showSnackBar(contentView,"Anda telah keluar dari aplikasi") }
            }
            if (!wakelockAcquired) {
                wakeLock.acquire((durasi.toLong() + 60) * 1000) // Akuisisi wakelock hanya selama durasi tambah 1 menit
                wakelockAcquired = true
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        webView.onActivityResult(requestCode, resultCode, intent)
        // ...
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        //progressbar
        progressBar.visibility = View.VISIBLE
    }

    override fun onPageFinished(url: String?) {
        progressBar.visibility = View.GONE
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {}

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {}
}