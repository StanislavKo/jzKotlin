package com.hsd.jz.android.pojo

import com.hsd.jz.android.pojo.data.Episode

data class SearchResult<T>(val offset: Int, val limit: Int, val list: MutableList<T>, val error: Boolean)