package com.hsd.jz.android.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hsd.jz.android.R
import android.content.Context
import java.util.*
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.hsd.jz.android.MainActivity
import com.hsd.jz.android.activity.WebEpisodeActivity
import com.hsd.jz.android.pojo.data.SearchTerm
import com.hsd.jz.android.utils.BackendUtils


class SearchTermsFragment: Fragment() {

    private val TAG = SearchTermsFragment::class.simpleName

    private lateinit var handler: Handler

    private lateinit var listSearchTerm: ListView
    private lateinit var progressBar: ProgressBar

    private val searchTerms = LinkedList<SearchTerm>()
    private lateinit var adapter: SearchTermDeleteListViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_terms, container, false)

        listSearchTerm = view.findViewById(R.id.listSearchTerm)
        progressBar = view.findViewById(R.id.progressBar)

        listSearchTerm.setOnItemClickListener { adapterView, view, position, l ->
            val searchTerm = adapterView.getItemAtPosition(position) as SearchTerm
            (this@SearchTermsFragment.activity as MainActivity).loadEpisodes(searchTerm.query)
        }

        handler = Handler()

        progressBar.visibility = View.VISIBLE
        initAdapter()

        return view
    }

    private fun initAdapter() {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                val p1 = BackendUtils.loadSearchTerms()
                handler.post({
                    if (p1.error) {
                    } else if (p1.list.size > 0) {
                        adapter = SearchTermDeleteListViewAdapter(this@SearchTermsFragment.context!!, R.layout.item_search_term_delete, R.id.text1, p1.list)
                        listSearchTerm.setAdapter(adapter)
                    }
                    (activity as MainActivity).hideSoftKeyboard()
                    progressBar.visibility = View.GONE
                })
                return null
            }
        }.execute()
    }

}
