package com.mgba.emulator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Test activity to diagnose startup crashes
 */
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}
