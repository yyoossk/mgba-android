package com.mgba.emulator

import android.app.Activity
import android.os.Bundle

/**
 * Test activity to diagnose startup crashes - minimal version
 */
class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_test)
        } catch (e: Exception) {
            // If layout fails, create minimal UI
            android.util.Log.e("TestActivity", "Layout error: ${e.message}", e)
            val tv = android.widget.TextView(this)
            tv.text = "mGBA Emulator Test\nError: ${e.message}"
            tv.textSize = 20f
            setContentView(tv)
        }
    }
}
