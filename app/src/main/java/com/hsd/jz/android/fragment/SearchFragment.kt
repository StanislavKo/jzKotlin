package com.hsd.jz.android.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hsd.jz.android.R
import com.hsd.jz.android.pojo.data.Episode
import android.content.Context
import android.content.Intent
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.hsd.jz.android.application.App
import com.hsd.jz.android.consts.PREF
import com.hsd.jz.android.pojo.SearchResult
import java.util.*
import com.google.gson.reflect.TypeToken
import com.hsd.jz.android.activity.WebEpisodeActivity


class SearchFragment: Fragment() {

    private val TAG = SearchFragment::class.simpleName

    private lateinit var editSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var listEpisode: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonMore: Button

    private var lastQuery = ""
    private val episodes = LinkedList<Episode>()
    private lateinit var adapter: EpisodeListViewAdapter

    private val loader = object : LoaderManager.LoaderCallbacks<SearchResult<Episode>> {
        public lateinit var asyncTaskLoader: EpisodeAsyncTaskLoader

        override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<SearchResult<Episode>> {
            asyncTaskLoader = EpisodeAsyncTaskLoader(this@SearchFragment.context!!)
//            asyncTaskLoader.forceLoad()
            Log.i(TAG, "onCreateLoader")
            return asyncTaskLoader
        }

        override fun onLoadFinished(p0: Loader<SearchResult<Episode>>, p1: SearchResult<Episode>) {
            if (p1.error) {
                asyncTaskLoader.offset = asyncTaskLoader.offset - 10
            } else if (p1.list.size == 0 && p1.offset > 0) {
                asyncTaskLoader.offset = asyncTaskLoader.offset - 10
            } else if (p1.offset == 0) {
                episodes.clear()
            }
            episodes.addAll(p1.list)
            buttonMore.visibility = if (p1.list.size > 0 || p1.error) View.VISIBLE else View.GONE
            buttonSearch.isEnabled = true
            buttonMore.isEnabled = true
            progressBar.visibility = View.GONE

            adapter.notifyDataSetChanged()
            Log.i(TAG, "onLoadFinish episodes.size=${p1?.list?.size} offset=${p1?.offset} limit=${p1?.limit} ");
        }

        override fun onLoaderReset(p0: Loader<SearchResult<Episode>>) {
            listEpisode.setAdapter(null)
            Log.i(TAG, "onLoaderReset")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i(TAG, "SearchFragemnt.onCreateView $savedInstanceState")

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        editSearch = view.findViewById(R.id.editSearch)
        buttonSearch = view.findViewById(R.id.buttonSearch)
        listEpisode = view.findViewById(R.id.listSearchTerm)
        progressBar = view.findViewById(R.id.progressBar)
        buttonMore = view.findViewById(R.id.buttonMore)

        buttonSearch.setOnClickListener({
            Log.i(TAG, "buttonSearch.onClick view=$it")
            if (editSearch.text.toString().length > 0 && lastQuery != editSearch.text.toString()) {
                loadInit(editSearch.text.toString())
            } else if (editSearch.text.toString().length > 0) {
                loadMore()
            }
        })
        buttonMore.setOnClickListener({
            Log.i(TAG, "buttonMore.onClick view=$it")
            loadMore()
        })
        listEpisode.setOnItemClickListener { adapterView, view, position, l ->
            val episode = adapterView.getItemAtPosition(position) as Episode
            val intent = Intent(context, WebEpisodeActivity::class.java)
            intent.putExtra("hash", episode.hash)
            startActivity(intent)
        }

        initAdapter()
        val loaderManager = loaderManager
        loaderManager.initLoader(0, null, loader);

        val query = arguments?.getString("query")
        if (query != null) {
            editSearch.setText(query)
            loadInit(query)
        } else {
            val prefs = App.context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            if (prefs.contains("search_query")) {
                editSearch.setText(prefs.getString("search_query", ""))
                lastQuery = prefs.getString("search_query", "")
                val jsonStr = prefs.getString("search_episodes", "")
                if (jsonStr != "") {
                    val founderListType = object : TypeToken<ArrayList<Episode>>() {}.type
                    episodes.addAll(Gson().fromJson(jsonStr, founderListType))
                }
                loader.asyncTaskLoader.query = prefs.getString("search_query", "")
                loader.asyncTaskLoader.offset = prefs.getInt("search_offset", 0)
                loader.asyncTaskLoader.limit = prefs.getInt("search_limit", 10)
            }
        }

        return view
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        Log.i(TAG, "SearchFragment.onSaveInstanceState()")
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "SearchFragment.onDestroyView()")
        val prefs = App.context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val jsonEpisodes = if (episodes == null) null else Gson().toJson(episodes)
        editor.putString("search_episodes", jsonEpisodes)
        editor.putString("search_query", lastQuery)
        editor.putInt("search_offset", loader.asyncTaskLoader.offset)
        editor.putInt("search_limit", loader.asyncTaskLoader.limit)
        editor.commit()
    }

    private fun initAdapter() {
        adapter = EpisodeListViewAdapter(this.context!!, android.R.layout.simple_list_item_1, android.R.id.text1, episodes)
        listEpisode.setAdapter(adapter)
    }

    private fun loadInit(query: String) {
        Log.i(TAG, "loadInit")
        progressBar.visibility = View.VISIBLE
        lastQuery = query
        loader.asyncTaskLoader.query = query
        loader.asyncTaskLoader.offset = 0
        loader.asyncTaskLoader.limit = 10
        loader.asyncTaskLoader.forceLoad()
    }

    private fun loadMore() {
        Log.i(TAG, "loadMore")
        buttonSearch.isEnabled = false
        buttonMore.isEnabled = false
        loader.asyncTaskLoader.offset = loader.asyncTaskLoader.offset + 10
        loader.asyncTaskLoader.limit = 10
        loader.asyncTaskLoader.forceLoad()
    }

}
