// app/src/main/java/com/mgba/emulator/MainActivity.kt

package com.mgba.emulator

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

/**
 * メインエミュレータ画面
 */
class MainActivity : AppCompatActivity() {

    private lateinit var emulatorCore: EmulatorCore
    private lateinit var surfaceView: SurfaceView
    private lateinit var btnPause: Button
    private lateinit var btnSave: Button
    private lateinit var btnLoad: Button

    private var renderingJob: Job? = null
    private var currentRomPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UIコンポーネント初期化
        initializeUI()

        // エミュレータコア初期化
        emulatorCore = EmulatorCore()
        if (!emulatorCore.initialize()) {
            Toast.makeText(this, "Failed to initialize emulator", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Intentからのデータ確認（GameLoaderActivityから渡される）
        val romPath = intent.getStringExtra("ROM_PATH")
        if (!romPath.isNullOrEmpty()) {
            currentRomPath = romPath
            loadAndStartROM(romPath)
        }
    }

    private fun initializeUI() {
        surfaceView = findViewById(R.id.surface_view)
        btnPause = findViewById(R.id.btn_pause)
        btnSave = findViewById(R.id.btn_save)
        btnLoad = findViewById(R.id.btn_load)

        // ボタンリスナー設定
        btnPause.setOnClickListener { togglePause() }
        btnSave.setOnClickListener { saveGame() }
        btnLoad.setOnClickListener { loadGame() }

        // SurfaceView設定
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                startRendering()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                stopRendering()
            }
        })
    }

    private fun loadAndStartROM(romPath: String) {
        lifecycleScope.launch {
            try {
                // ROM読み込み
                if (emulatorCore.loadROM(romPath)) {
                    // エミュレーション開始
                    if (emulatorCore.start()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Emulation started",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to start emulation", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load ROM", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRendering() {
        if (renderingJob == null || !renderingJob!!.isActive) {
            renderingJob = lifecycleScope.launch {
                renderLoop()
            }
        }
    }

    private fun stopRendering() {
        renderingJob?.cancel()
        renderingJob = null
    }

    private suspend fun renderLoop() {
        val frameBufferInfo = emulatorCore.getFrameBufferInfo()
        val width = frameBufferInfo.width
        val height = frameBufferInfo.height

        // フレームバッファをByteBufferでアクセス（ネイティブメモリから）
        val buffer = ByteBuffer.allocateDirect(width * height * 2) // RGB565

        while (true) {
            try {
                val holder = surfaceView.holder
                val canvas: Canvas? = holder.lockCanvas()

                if (canvas != null) {
                    // フレームバッファをBitmap形式で描画
                    // TODO: ネイティブフレームバッファから画像データを取得・変換して描画

                    val paint = Paint().apply {
                        isAntiAlias = true
                    }

                    // プレースホルダー：ブラック画面
                    canvas.drawColor(android.graphics.Color.BLACK)

                    holder.unlockCanvasAndPost(canvas)
                }

                // フレームレート制御（60 FPS）
                Thread.sleep(16)

            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }
    }

    private fun togglePause() {
        lifecycleScope.launch {
            val isPaused = emulatorCore.pause()
            btnPause.text = if (isPaused) "Resume" else "Pause"

            if (isPaused) {
                emulatorCore.resume()
                btnPause.text = "Pause"
            }
        }
    }

    private fun saveGame() {
        lifecycleScope.launch {
            try {
                val saveDir = getExternalFilesDir("saves")
                saveDir?.mkdirs()
                val savePath = "$saveDir/save_slot_1.sav"

                if (emulatorCore.saveState(savePath)) {
                    Toast.makeText(this@MainActivity, "Game saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Save failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGame() {
        lifecycleScope.launch {
            try {
                val saveDir = getExternalFilesDir("saves")
                val savePath = "$saveDir/save_slot_1.sav"

                if (emulatorCore.loadState(savePath)) {
                    Toast.makeText(this@MainActivity, "Game loaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Load failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ========== キー入力処理 ==========

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return handleKeyEvent(keyCode, true) || super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return handleKeyEvent(keyCode, false) || super.onKeyUp(keyCode, event)
    }

    private fun handleKeyEvent(keyCode: Int, isPressed: Boolean): Boolean {
        val gbaKey = when (keyCode) {
            KeyEvent.KEYCODE_Z -> GBAKey.A           // Zキー = A
            KeyEvent.KEYCODE_X -> GBAKey.B           // Xキー = B
            KeyEvent.KEYCODE_DPAD_UP -> GBAKey.UP
            KeyEvent.KEYCODE_DPAD_DOWN -> GBAKey.DOWN
            KeyEvent.KEYCODE_DPAD_LEFT -> GBAKey.LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> GBAKey.RIGHT
            KeyEvent.KEYCODE_SHIFT_LEFT -> GBAKey.SELECT
            KeyEvent.KEYCODE_ENTER -> GBAKey.START
            KeyEvent.KEYCODE_Q -> GBAKey.L          // Qキー = L
            KeyEvent.KEYCODE_W -> GBAKey.R          // Wキー = R
            else -> return false
        }

        emulatorCore.setKeyState(gbaKey, isPressed)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRendering()
        emulatorCore.stop()
        emulatorCore.destroy()
    }
}
