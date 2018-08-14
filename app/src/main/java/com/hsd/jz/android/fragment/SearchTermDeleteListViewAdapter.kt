package com.hsd.jz.android.fragment

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.hsd.jz.android.R
import com.hsd.jz.android.application.App
import com.hsd.jz.android.pojo.data.Episode
import com.hsd.jz.android.pojo.data.SearchTerm
import com.hsd.jz.android.utils.BackendUtils

class SearchTermDeleteListViewAdapter(context: Context, resource: Int, textViewResourceId: Int, objects: MutableList<SearchTerm>)
    : ArrayAdapter<SearchTerm>(context, resource, textViewResourceId, objects) {

    private val TAG = SearchTermDeleteListViewAdapter::class.simpleName


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = inflater.inflate(R.layout.item_search_term_delete, parent, false)
        val text1 = convertView.findViewById<TextView>(R.id.text1)
        val contDelete = convertView.findViewById<LinearLayout>(R.id.contDelete)
        val iconDelete = convertView.findViewById<ImageView>(R.id.iconDelete)

        val searchTerm = getItem(position)
        text1.text = searchTerm.query
        contDelete.setOnClickListener {
            Log.i(TAG, "onDelete")

            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg p0: Void?): Void? {
                    val result = BackendUtils.deleteSearchTerm(searchTerm.query)

                    val handler = Handler(Looper.getMainLooper())
                    handler.post(Runnable {
                        if (!result.error) {
                            this@SearchTermDeleteListViewAdapter.remove(searchTerm)
                            this@SearchTermDeleteListViewAdapter.notifyDataSetChanged()
                        }
                    })

                    return null
                }
            }.execute()

        }

        return convertView
    }

}

