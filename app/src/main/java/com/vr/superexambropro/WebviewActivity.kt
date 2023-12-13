package com.vr.superexambropro

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.ProgressBar
import im.delight.android.webview.AdvancedWebView

class WebviewActivity : AppCompatActivity(), AdvancedWebView.Listener {

    private lateinit var webView: AdvancedWebView
    private var url = "https://www.google.com"
    private var progressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initView()
        initIntent()
        initWebView()

    }

    private fun initView(){
        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progressBar)
    }
    private fun initIntent(){
        url = intent.getStringExtra("url").toString()
    }
    private fun initWebView(){

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
    }
    private fun loadWebPage(url: String) {
        webView.loadUrl(url)
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        progressBar?.visibility = ProgressBar.VISIBLE
    }

    override fun onPageFinished(url: String?) {
        progressBar?.visibility = ProgressBar.GONE
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
        TODO("Not yet implemented")
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun onExternalPageRequest(url: String?) {
        TODO("Not yet implemented")
    }
}