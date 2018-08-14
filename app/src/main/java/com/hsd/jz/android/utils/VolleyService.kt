package com.hsd.jz.android.utils

import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.hsd.jz.android.application.App


object VolleyService {

    val requestQueue: RequestQueue by lazy { Volley.newRequestQueue(App.context) }

}