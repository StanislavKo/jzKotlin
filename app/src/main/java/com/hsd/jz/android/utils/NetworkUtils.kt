package com.hsd.jz.android.utils

import android.content.Context
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import com.hsd.jz.android.application.App


object NetworkUtils {

    private val TAG = NetworkUtils::class.simpleName

    fun wifi(): Boolean {
        val cm = App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        return false;
//        return activeNetwork.type == ConnectivityManager.TYPE_WIFI
    }

}
