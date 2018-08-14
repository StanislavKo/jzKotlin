package com.hsd.jz.android.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hsd.jz.android.R
import com.hsd.jz.android.consts.PREF
import com.hsd.jz.android.consts.PREF_JWT
import java.io.BufferedReader
import java.io.InputStreamReader
import android.net.http.SslError
import android.graphics.Bitmap
import android.webkit.*


class WebEpisode2Activity : AppCompatActivity() {

    private val TAG = WebEpisode2Activity::class.simpleName

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web_episode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.hide()

        webView = findViewById(R.id.webView)
        webView.getSettings().setAppCacheEnabled(true)
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);


//        webView.setWebViewClient(WebViewClient())
//        webView.setWebChromeClient(object : WebChromeClient() {
//            override fun onProgressChanged(view: WebView, progress: Int) {
//                Log.d(TAG, "Progress = $progress")
//            }
//        })

        val webClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
            override fun onPageStarted(view: WebView, url: String, facIcon: Bitmap?) {
            }
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.proceed()
            }
            override fun onPageFinished(view: WebView, url: String) {
            }
        }
        webView.setWebViewClient(webClient)

//        val prefs = applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
//        if (prefs.contains(PREF_JWT)) {
//            val cookieManager = CookieManager.getInstance()
//            cookieManager.setAcceptCookie(true)
//            val cookieString = "jwt=" + prefs.getString(PREF_JWT, "asdf") + "; Domain=jzpro.ru"
//            cookieManager.setCookie("jzpro.ru", cookieString)
//        }

        var html = convertStreamToString(assets.open("episode.html"))
        html = html.replace("__descriptionHtml__", intent.getStringExtra("descriptionHtml"))
        html = html.replace("__iframeHtml__", intent.getStringExtra("iframe"))
        Log.i(TAG, "html = $html")
//        webView.loadData(html, "text/html", "UTF-8")
        webView.loadDataWithBaseURL("", html, "text/html", "UTF-8","")
    }

    private fun convertStreamToString(inputStream: java.io.InputStream): String {
        val s = java.util.Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

}
