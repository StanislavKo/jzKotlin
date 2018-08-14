package com.hsd.jz.android

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hsd.jz.android.application.App
import com.hsd.jz.android.consts.PREF
import com.hsd.jz.android.consts.PREF_FRAGMENT
import com.hsd.jz.android.consts.PREF_JWT
import com.hsd.jz.android.fragment.FavoritesFragment
import com.hsd.jz.android.fragment.SearchFragment
import com.hsd.jz.android.fragment.SearchTermsFragment
import com.hsd.jz.android.pojo.MainActivityFragmentType
import com.hsd.jz.android.utils.BackendUtils
import android.R.attr.fragment
import android.support.v4.content.ContextCompat.getSystemService
import android.app.Activity
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.simpleName

    private lateinit var mAuth: FirebaseAuth

    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentContainer = findViewById(R.id.fragmentContainer)
        findViewById<BottomNavigationView>(R.id.navigation).setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val prefs = applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (!prefs.contains(PREF_JWT)) {
            val currentUser = mAuth.getCurrentUser()
            if (currentUser == null) {
                signinAnonymously()
            } else {
                updateUI(currentUser)
                BackendUtils.loadJwt(currentUser)
            }
        }

        val fragType = MainActivityFragmentType.valueOf(prefs.getString(PREF_FRAGMENT, MainActivityFragmentType.SEARCH.name))
        showFragment(fragType)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        supportFragmentManager.putFragment(outState, "myFragmentName", mContent)
        Log.i(TAG, "MainActivity.onSaveInstanceState")
    }

    fun loadEpisodes(query: String) {
        val prefs = App.context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(PREF_FRAGMENT, MainActivityFragmentType.SEARCH.name)
        editor.commit()
        navigation.selectedItemId = R.id.navigation_search

        val fragment = SearchFragment()
        val arguments = Bundle()
        arguments.putString("query", query)
        fragment.arguments = arguments
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }

    fun hideSoftKeyboard() {
        if (currentFocus == null) {
            return
        }
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager!!.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }

    private fun signinAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInAnonymously:success")
                val user = mAuth.getCurrentUser()
                updateUI(user)
                if (user != null) {
                    BackendUtils.loadJwt(user)
                }
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInAnonymously:failure", task.exception)
                Toast.makeText(this@MainActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {
        val prefs = App.context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        lateinit var fragType: MainActivityFragmentType
        when (it.itemId) {
            R.id.navigation_search -> {
                fragType = MainActivityFragmentType.SEARCH
            }
            R.id.navigation_favorites -> {
                fragType = MainActivityFragmentType.FAVORITES
            }
            R.id.navigation_search_terms -> {
                fragType = MainActivityFragmentType.SEARCH_TERMS
            }
        }
        editor.putString(PREF_FRAGMENT, fragType.name)
        editor.commit()
        showFragment(fragType)
        true
    }

    private fun showFragment(fragType: MainActivityFragmentType) {
        lateinit var fragment: Fragment
        when (fragType) {
            MainActivityFragmentType.SEARCH -> {
                fragment = SearchFragment()
            }
            MainActivityFragmentType.FAVORITES -> {
                fragment = FavoritesFragment()
            }
            MainActivityFragmentType.SEARCH_TERMS -> {
                fragment = SearchTermsFragment()
            }
        }
//        getSupportFragmentManager().beginTransaction().remove()
//        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment, fragType.name).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }

}
