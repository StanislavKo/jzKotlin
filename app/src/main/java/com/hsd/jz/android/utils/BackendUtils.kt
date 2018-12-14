package com.hsd.jz.android.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.google.firebase.auth.FirebaseUser
import com.hsd.jz.android.application.App
import com.hsd.jz.android.consts.*
import com.hsd.jz.android.pojo.SearchResult
import com.hsd.jz.android.pojo.SearchResultGeneric
import com.hsd.jz.android.pojo.data.Episode
import com.hsd.jz.android.pojo.data.SearchTerm
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

object BackendUtils {

    private val TAG = BackendUtils::class.simpleName

    fun loadJwt(user: FirebaseUser) {
        Log.w(TAG, "loadJwt")

        val jsonObj = JSONObject()
        jsonObj.put("uid", user.uid);
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST, URL_SIGNIN, jsonObj,
                Response.Listener { response ->
                    Log.e("loadJwt", "response:" + response.toString())
                    val prefs = App.context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString(PREF_JWT, response.getJSONObject("headers").getString(HEADER_JWT))
                    editor.commit()

                },
                Response.ErrorListener { error ->
                    Log.e("loadJwt", "error: $error")
                }
        ) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                try {
                    Log.e(TAG, "parseNetworkResponse $response.data, $response.headers")
                    val jsonString = String(response.data)
                    val jsonResponse = JSONObject(jsonString)
                    jsonResponse.put("headers", JSONObject(response.headers))
                    return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: UnsupportedEncodingException) {
                    return Response.error<JSONObject>(ParseError(e))
                } catch (je: JSONException) {
                    return Response.error<JSONObject>(ParseError(je))
                }

            }
        }
        VolleyService.requestQueue.add(jsonObjectRequest)
//        VolleyService.requestQueue.start()
    }

    fun checkSignedIn(isSignedIn: Boolean) {
        if (!isSignedIn) {
            val prefs = App.context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.remove(PREF_JWT)
            editor.commit()
        }
    }

    fun loadEpisodes(query: String?, offset: Int, limit: Int): SearchResult<Episode> {
        Log.i(TAG, "loadEpisodes() $query $offset $limit")
        if (query == null) {
            return SearchResult(0, 10, ArrayList<Episode>(), false)
        }
        val queryEncoded = URLEncoder.encode(query, "UTF-8")
        val future = RequestFuture.newFuture<JSONObject>()
        val jsonObjectRequest = MyJsonObjectRequest(Request.Method.GET, "$URL_EPISODES/$offset/$limit/$queryEncoded", null, future, future)
        VolleyService.requestQueue.add(jsonObjectRequest)
//        VolleyService.requestQueue.start()

        var repeat = 3
        while (true) {
            try {
                val response = future.get() // this will block
                checkSignedIn(response.getBoolean("loggedin"))
                val episodes = LinkedList<Episode>()
                val episodeJsonArray = response.getJSONObject("episodeList").getJSONArray("data")
                val offset2 = response.getJSONObject("episodeList").getLong("offset")
                val limit2 = response.getJSONObject("episodeList").getLong("limit")
                val count = response.getJSONObject("episodeList").getLong("count")
                Log.i(TAG, "loadEpisodes count=$count")
                for (i in 0 until episodeJsonArray.length()) {
                    val episodeJsonObj = episodeJsonArray.getJSONObject(i)
                    episodes.add(Episode(episodeJsonObj.getString("hash"), episodeJsonObj.getString("title"), episodeJsonObj.getString("created")))
//                        , episodeJsonObj.getString("iframe"), episodeJsonObj.getString("description"), episodeJsonObj.getString("descriptionHtml")))
                }
                return SearchResult(offset, limit, episodes, false)
            } catch (error: Exception) {
                Log.i(TAG, "loadEpisodes $error")
                if (repeat-- > 0) {
                    VolleyService.requestQueue.add(jsonObjectRequest)
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post(Runnable { Toast.makeText(App.context, "Проблема с сетью", Toast.LENGTH_SHORT).show() })
                    return SearchResult(0, 10, ArrayList<Episode>(), true)
                }
            }
        }
    }

    fun loadFavorites(): SearchResult<Episode> {
        Log.i(TAG, "loadFavorites()")
        val future = RequestFuture.newFuture<JSONObject>()
        val jsonObjectRequest = MyJsonObjectRequest(Request.Method.GET, "$URL_FAVORITES", null, future, future)
        VolleyService.requestQueue.add(jsonObjectRequest)
//        VolleyService.requestQueue.start()

        var repeat = 3
        while (true) {
            try {
                val response = future.get() // this will block
                checkSignedIn(response.getBoolean("loggedin"))
                val episodes = LinkedList<Episode>()
                val episodeJsonArray = response.getJSONObject("episodeList").getJSONArray("data")
                val count = response.getJSONObject("episodeList").getLong("count")
                Log.i(TAG, "loadFavorites count=$count")
                for (i in 0 until episodeJsonArray.length()) {
                    val episodeJsonObj = episodeJsonArray.getJSONObject(i)
                    episodes.add(Episode(episodeJsonObj.getString("hash"), episodeJsonObj.getString("title"), episodeJsonObj.getString("created")))
    //                        , episodeJsonObj.getString("iframe"), episodeJsonObj.getString("description"), episodeJsonObj.getString("descriptionHtml")))
                }
                return SearchResult(-1, -1, episodes, false)
            } catch (error: Exception) {
                Log.i(TAG, "loadFavorites $error")
                if (repeat-- > 0) {
                    VolleyService.requestQueue.add(jsonObjectRequest)
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post(Runnable { Toast.makeText(App.context, "Проблема с сетью", Toast.LENGTH_SHORT).show() })
                    return SearchResult(-1, -1, ArrayList<Episode>(), true)
                }
            }
        }
    }

    fun deleteFavorite(hash: String): SearchResultGeneric {
        Log.i(TAG, "deleteFavorite()")
        val future = RequestFuture.newFuture<JSONObject>()
        val jsonObjectRequest = MyJsonObjectRequest(Request.Method.GET, "$URL_FAVORITE_DELETE$hash" , null, future, future)
        VolleyService.requestQueue.add(jsonObjectRequest)
//        VolleyService.requestQueue.start()

        var repeat = 3
        while (true) {
            try {
                val response = future.get() // this will block
                checkSignedIn(response.getBoolean("loggedin"))
                return SearchResultGeneric(false)
            } catch (error: Exception) {
                Log.i(TAG, "deleteFavorite $error")
                if (repeat-- > 0) {
                    VolleyService.requestQueue.add(jsonObjectRequest)
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post(Runnable { Toast.makeText(App.context, "Проблема с сетью", Toast.LENGTH_SHORT).show() })
                    return SearchResultGeneric(true)
                }
            }
        }
    }

    fun loadSearchTerms(): SearchResult<SearchTerm> {
        Log.i(TAG, "loadSearchTerms()")
        val future = RequestFuture.newFuture<JSONObject>()
        val jsonObjectRequest = MyJsonObjectRequest(Request.Method.GET, "$URL_SEARCH_TERMS", null, future, future)
        VolleyService.requestQueue.add(jsonObjectRequest)
//        VolleyService.requestQueue.start()

        var repeat = 3
        while (true) {
            try {
                val response = future.get() // this will block
                checkSignedIn(response.getBoolean("loggedin"))
                val searchTerms = LinkedList<SearchTerm>()
                val searchTermJsonArray = response.getJSONArray("searchTermList")
                Log.i(TAG, "loadSearchTerms count=${searchTermJsonArray.length()}")
                for (i in 0 until searchTermJsonArray.length()) {
                    val searchTerm = searchTermJsonArray.getString(i)
                    searchTerms.add(SearchTerm(URLDecoder.decode(searchTerm, "UTF-8")))
                }
                return SearchResult(-1, -1, searchTerms, false)
            } catch (error: Exception) {
                Log.i(TAG, "loadSearchTerms $error")
                if (repeat-- > 0) {
                    VolleyService.requestQueue.add(jsonObjectRequest)
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post(Runnable { Toast.makeText(App.context, "Проблема с сетью", Toast.LENGTH_SHORT).show() })
                    return SearchResult(-1, -1, ArrayList<SearchTerm>(), true)
                }
            }
        }
    }

    fun deleteSearchTerm(query: String): SearchResultGeneric {
        Log.i(TAG, "deleteSearchTerm()")
        var queryEncoded = URLEncoder.encode(query, "UTF-8")
        queryEncoded = queryEncoded.replace("+", "%20")
        val future = RequestFuture.newFuture<JSONObject>()
        val jsonObjectRequest = MyJsonObjectRequest(Request.Method.GET, "$URL_SEARCH_TERMS_DELETE$queryEncoded" , null, future, future)
        VolleyService.requestQueue.add(jsonObjectRequest)
//        VolleyService.requestQueue.start()

        var repeat = 3
        while (true) {
            try {
                val response = future.get() // this will block
                checkSignedIn(response.getBoolean("loggedin"))
                return SearchResultGeneric(false)
            } catch (error: Exception) {
                Log.i(TAG, "deleteSearchTerm $error")
                if (repeat-- > 0) {
                    VolleyService.requestQueue.add(jsonObjectRequest)
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post(Runnable { Toast.makeText(App.context, "Проблема с сетью", Toast.LENGTH_SHORT).show() })
                    return SearchResultGeneric(true)
                }
            }
        }
    }

    class MyJsonObjectRequest(method: Int, url: String, jsonRequest: JSONObject?, listener: Listener<JSONObject>, errorListener: Response.ErrorListener?)
        : JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            val prefs = App.context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            if (prefs.contains(PREF_JWT)) {
                headers["Authorization"] = prefs.getString(PREF_JWT, "")
            }
            return headers
        }
    }

}
