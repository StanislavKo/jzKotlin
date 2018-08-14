package com.hsd.jz.android.fragment

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.hsd.jz.android.pojo.data.Episode

class EpisodeListViewAdapter(context: Context, resource: Int, textViewResourceId: Int, objects: MutableList<Episode>)
    : ArrayAdapter<Episode>(context, resource, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
        val text1 = convertView.findViewById<TextView>(android.R.id.text1)
        val text2 = convertView.findViewById<TextView>(android.R.id.text2)

        val episode = getItem(position)
        text1.text = episode.title
        text2.text = episode.created

        return convertView
    }

}

