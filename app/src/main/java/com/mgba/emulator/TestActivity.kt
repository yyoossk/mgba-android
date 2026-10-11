package com.mgba.emulator

import android.app.Activity
import android.os.Bundle
import android.util.Log

/**
 * Absolute minimal test activity - no UI, just logging
 */
class TestActivity : Activity() {
    companion object {
        private const val TAG = "mGBA-Test"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "=== TestActivity.onCreate() called ===")
        Log.i(TAG, "Application started successfully!")
        Log.i(TAG, "Package: ${packageName}")
        Log.i(TAG, "Version: ${packageManager.getPackageInfo(packageName, 0).versionName}")
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "TestActivity.onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "TestActivity.onResume() called")
    }
}
