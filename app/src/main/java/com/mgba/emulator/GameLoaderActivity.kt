package com.mgba.emulator

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

/**
 * Minimal game loader activity
 */
class GameLoaderActivity : Activity() {
    companion object {
        private const val TAG = "GameLoaderActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "GameLoaderActivity.onCreate() called")

        try {
            // Create a simple TextView without layout files
            val tv = TextView(this)
            tv.text = "Game Loader\nPress back to exit"
            tv.textSize = 24f
            setContentView(tv)
            Log.i(TAG, "GameLoaderActivity created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in GameLoaderActivity: ${e.message}", e)
            finish()
        }
    }
}
