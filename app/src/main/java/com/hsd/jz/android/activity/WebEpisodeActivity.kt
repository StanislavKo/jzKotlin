package com.hsd.jz.android.activity

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import com.hsd.jz.android.R
import com.hsd.jz.android.consts.PREF
import com.hsd.jz.android.consts.PREF_JWT

class WebEpisodeActivity : AppCompatActivity() {

    private val TAG = WebEpisodeActivity::class.simpleName

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web_episode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.hide()

        webView = findViewById(R.id.webView)
        webView.getSettings().setJavaScriptEnabled(true)
        webView.getSettings().setAppCacheEnabled(true)
        webView.getSettings().setDomStorageEnabled(true)

        val prefs = applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (prefs.contains(PREF_JWT)) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            val cookieString = "jwt=" + prefs.getString(PREF_JWT, "asdf") + "; Domain=jzpro.ru"
            cookieManager.setCookie("jzpro.ru", cookieString)
        }

        val url = "http://jzpro.ru/episode/" + intent.getStringExtra("hash")
        Log.i(TAG, "url=$url")
        webView.loadUrl(url)
    }

}
