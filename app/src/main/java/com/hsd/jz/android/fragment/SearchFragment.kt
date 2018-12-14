package com.hsd.jz.android.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hsd.jz.android.R
import com.hsd.jz.android.activity.WebEpisodeActivity
import com.hsd.jz.android.application.App
import com.hsd.jz.android.consts.PREF
import com.hsd.jz.android.pojo.SearchResult
import com.hsd.jz.android.pojo.data.Episode
import java.util.*


class SearchFragment : Fragment() {

    private val TAG = SearchFragment::class.simpleName
    private val VOICE_RECOGNITION_REQUEST_CODE = 23782
    private val REQUEST_INTERNET_PERMISSION_RESULT = 24781
    private val REQUEST_AUDIO_PERMISSION_RESULT = 24782

    private lateinit var editSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var listEpisode: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonMore: Button
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var dialogVoice: DialogVoiceFragment

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
        editSearch.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= editSearch.getRight() - editSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 8) {
                    startVoiceRecognitionActivity()
                    return@OnTouchListener true
                }
            }
            false
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this@SearchFragment.context!!, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.INTERNET), REQUEST_INTERNET_PERMISSION_RESULT);
            }
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

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                Log.i(TAG, "onReadyForSpeech")
            }

            override fun onRmsChanged(p0: Float) {
//                Log.i(TAG, "onRmsChanged")
            }

            override fun onBufferReceived(p0: ByteArray?) {
                Log.i(TAG, "onBufferReceived")
            }

            override fun onPartialResults(p0: Bundle?) {
                Log.i(TAG, "onPartialResults")
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.i(TAG, "onEvent")
            }

            override fun onBeginningOfSpeech() {
                Log.i(TAG, "onBeginningOfSpeech")
            }

            override fun onEndOfSpeech() {
                Log.i(TAG, "onEndOfSpeech")
            }

            override fun onError(p0: Int) {
                Log.i(TAG, "onError p0=$p0")
            }

            override fun onResults(p0: Bundle?) {
                Log.i(TAG, "onResults")
                for (key in p0!!.keySet()) {
                    Log.i(TAG, "onResults key=$key value=${p0[key]}")
                }
                if (getFragmentManager()!!.findFragmentByTag("dialogVoice") != null) {
                    val matches = p0!!["results_recognition"] as ArrayList<String>
                    Log.i(TAG, "onResults() matches.size=${matches.size}")
                    if (matches.size > 0) {
                        editSearch.setText(matches[0])
                    }
                }
                speechRecognizer.stopListening()
                dialogVoice.dismiss()
            }

        })

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult() requestCode=$requestCode resultCode=$resultCode ")

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            Log.i(TAG, "onActivityResult() matches.size=${matches.size}")
            if (matches.size > 0) {
                editSearch.setText(matches[0])
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_AUDIO_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognitionActivityImpl()
            }
        } else if (requestCode == REQUEST_INTERNET_PERMISSION_RESULT) {
            initAdapter()
        }
    }

    // implementation

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

    private fun startVoiceRecognitionActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this@SearchFragment.context!!, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognitionActivityImpl()
            } else {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO_PERMISSION_RESULT);
            }
        } else {
            startVoiceRecognitionActivityImpl()
        }
    }

    private fun startVoiceRecognitionActivityImpl() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Что вы ищите?")
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
//        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity!!.packageName)
        speechRecognizer.startListening(intent);
        showVoiceDialog()
//        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
    }

    private fun showVoiceDialog() {
        dialogVoice = DialogVoiceFragment()
        dialogVoice.show(fragmentManager, "dialogVoice")
    }

}
