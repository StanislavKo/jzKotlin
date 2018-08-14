package com.hsd.jz.android.fragment

import android.content.Context
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import com.hsd.jz.android.pojo.SearchResult
import com.hsd.jz.android.pojo.data.Episode
import com.hsd.jz.android.utils.BackendUtils

class EpisodeAsyncTaskLoader(context: Context) : AsyncTaskLoader<SearchResult<Episode>>(context) {

    public var query: String? = null
    public var offset: Int = 0
    public var limit: Int = 10

    init {
        Log.i(TAG, "init Asynctask Loader")
    }

    override fun loadInBackground(): SearchResult<Episode> {
        val newEpisodes = BackendUtils.loadEpisodes(query, offset, limit)
        Log.i(TAG, "loadInBackground ${newEpisodes.list.size} error=${newEpisodes.error}")

        try {
            synchronized(this) {
                Log.i(TAG, "load in background")
            }
        } catch (e: Exception) {
            e.message
        }

        return newEpisodes
    }

    override fun deliverResult(data: SearchResult<Episode>?) {
        super.deliverResult(data)
        Log.i(TAG, "deliver Result  ${data?.list?.size}")
    }

    companion object {
        private val TAG = EpisodeAsyncTaskLoader::class.java.simpleName
    }
}