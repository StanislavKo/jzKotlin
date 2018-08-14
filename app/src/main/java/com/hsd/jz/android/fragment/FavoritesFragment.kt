package com.hsd.jz.android.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hsd.jz.android.R
import com.hsd.jz.android.pojo.data.Episode
import android.content.Context
import java.util.*
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.hsd.jz.android.MainActivity
import com.hsd.jz.android.activity.WebEpisodeActivity
import com.hsd.jz.android.utils.BackendUtils


class FavoritesFragment: Fragment() {

    private val TAG = FavoritesFragment::class.simpleName

    private lateinit var handler: Handler

    private lateinit var listEpisode: ListView
    private lateinit var progressBar: ProgressBar

    private val episodes = LinkedList<Episode>()
    private lateinit var adapter: EpisodeDeleteListViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        listEpisode = view.findViewById(R.id.listSearchTerm)
        progressBar = view.findViewById(R.id.progressBar)

        listEpisode.setOnItemClickListener { adapterView, view, position, l ->
            val episode = adapterView.getItemAtPosition(position) as Episode
            val intent = Intent(context, WebEpisodeActivity::class.java)
            intent.putExtra("hash", episode.hash)
            startActivity(intent)
        }

        handler = Handler()

        progressBar.visibility = View.VISIBLE
        initAdapter()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0) // InputMethodManager.HIDE_NOT_ALWAYS
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun initAdapter() {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                val p1 = BackendUtils.loadFavorites()
                handler.post({
                    if (p1.error) {
                    } else if (p1.list.size > 0) {
                        adapter = EpisodeDeleteListViewAdapter(this@FavoritesFragment.context!!, R.layout.item_episode_delete, R.id.text1, p1.list)
                        listEpisode.setAdapter(adapter)
                    }
                    (activity as MainActivity).hideSoftKeyboard()
                    progressBar.visibility = View.GONE
                })
                return null
            }
        }.execute()
    }

}
