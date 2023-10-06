package com.vr.superexambropro.activity.siswa

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.superexambropro.R
import com.vr.superexambropro.helper.showSnackBar
import com.vr.superexambropro.helper.startTimer
import com.vr.superexambropro.helper.updateFirebase
import java.util.Timer
import java.util.TimerTask


class MulaiActivity() : AppCompatActivity() {
    lateinit var btnRefresh :ImageButton
    lateinit var btnSelesai : LinearLayout
    lateinit var tvTimer  : TextView
    lateinit var contentView  : RelativeLayout
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnYakin: Button
    private lateinit var btnKembali: Button
    private lateinit var lyKonfirm: RelativeLayout
    var durasi = ""
    var idUjian = ""
    var url =""
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
    }
    private fun initView(){
        btnRefresh = findViewById(R.id.btnRefresh)
        btnSelesai = findViewById(R.id.btnSelesai)
        tvTimer = findViewById(R.id.tvTimer)
        contentView = findViewById(R.id.contentView)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        btnYakin = findViewById(R.id.btnYakin)
        btnKembali = findViewById(R.id.btnKembali)
        lyKonfirm = findViewById(R.id.lyKonfirm)
        lyKonfirm.visibility = View.GONE
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
            updateFirebase("DetailActivity", FirebaseFirestore.getInstance(), "ujian",
                idUjian, hashMapOf("status" to "Selesai"))
            { showSnackBar(contentView,"Berhasil menyimpan data") }
        }
    }
    private fun initIntent(){
        durasi = intent.getStringExtra("durasi").toString()
        idUjian = intent.getStringExtra("documentId").toString()
        url = intent.getStringExtra("url").toString()
    }
    private fun initTimer(){
        startTimer(contentView,durasi.toInt(), tvTimer, idUjian,this,false,this)
    }
    fun initWebview() {
        /// Konfigurasi WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        //check url ada https atau http tidak di depannya
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        loadWebPage(url)
        Log.d("TAG", "initWebview: $url")
        // Mengaktifkan WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                btnRefresh.isEnabled = true
            }
        }

        // Mengaktifkan WebChromeClient untuk loading bar
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
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
    override fun onBackPressed() {}
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
        super.onDestroy()

        // Hapus penerima saat aktivitas dihancurkan
        if (powerButtonReceiver != null) {
            unregisterReceiver(powerButtonReceiver)
        }
    }

}