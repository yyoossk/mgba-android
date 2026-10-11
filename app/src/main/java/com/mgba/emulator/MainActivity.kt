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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

/**
 * メインエミュレータ画面（Fragment-based architecture）
 */
class MainActivity : AppCompatActivity() {

    private lateinit var emulatorCore: EmulatorCore
    private lateinit var surfaceView: SurfaceView
    private lateinit var btnPause: Button
    private lateinit var btnSave: Button
    private lateinit var btnLoad: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout

    private var renderingJob: Job? = null
    private var currentRomPath: String? = null
    private var currentFragment: Fragment? = null

    enum class Screen {
        GAME, SETTINGS, CHEATS, SAVES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create main layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Create game screen container
        val gameScreenLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        // UIコンポーネント初期化
        initializeGameUI(gameScreenLayout)

        // Fragment container
        fragmentContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            id = android.view.View.generateViewId()
            visibility = android.view.View.GONE
        }

        // Bottom navigation
        bottomNavigation = BottomNavigationView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Create menu items programmatically
            val menu = this.menu
            menu.add(0, 1, 0, getString(R.string.app_name))
                .setIcon(android.R.drawable.ic_dialog_info)
            menu.add(0, 2, 1, getString(R.string.menu_settings))
                .setIcon(android.R.drawable.ic_menu_manage)
            menu.add(0, 3, 2, getString(R.string.cheats))
                .setIcon(android.R.drawable.ic_menu_view)
            menu.add(0, 4, 3, getString(R.string.save_states))
                .setIcon(android.R.drawable.ic_menu_save)

            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    1 -> showScreen(Screen.GAME)
                    2 -> showScreen(Screen.SETTINGS)
                    3 -> showScreen(Screen.CHEATS)
                    4 -> showScreen(Screen.SAVES)
                    else -> false
                }
            }
        }

        // Add views to main layout
        mainLayout.addView(gameScreenLayout)
        mainLayout.addView(fragmentContainer)
        mainLayout.addView(bottomNavigation)

        setContentView(mainLayout)

        // エミュレータコア初期化
        emulatorCore = EmulatorCore()
        if (!emulatorCore.initialize()) {
            Toast.makeText(this, getString(R.string.emulation_failed), Toast.LENGTH_SHORT).show()
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

    private fun initializeGameUI(container: LinearLayout) {
        surfaceView = SurfaceView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val controlLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        btnPause = Button(this).apply {
            text = getString(R.string.btn_pause)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { togglePause() }
        }

        btnSave = Button(this).apply {
            text = getString(R.string.btn_save)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { saveGame() }
        }

        btnLoad = Button(this).apply {
            text = getString(R.string.btn_load)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { loadGame() }
        }

        controlLayout.addView(btnPause)
        controlLayout.addView(btnSave)
        controlLayout.addView(btnLoad)

        container.addView(surfaceView)
        container.addView(controlLayout)

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

    private fun showScreen(screen: Screen): Boolean {
        return when (screen) {
            Screen.GAME -> {
                fragmentContainer.visibility = android.view.View.GONE
                surfaceView.visibility = android.view.View.VISIBLE
                startRendering()
                true
            }
            Screen.SETTINGS -> {
                showFragment(SettingsFragment.newInstance())
                true
            }
            Screen.CHEATS -> {
                showFragment(CheatsFragment.newInstance())
                true
            }
            Screen.SAVES -> {
                showFragment(SavesFragment.newInstance())
                true
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        currentFragment = fragment
        surfaceView.visibility = android.view.View.GONE
        stopRendering()
        fragmentContainer.visibility = android.view.View.VISIBLE

        supportFragmentManager.commit {
            replace(fragmentContainer.id, fragment)
        }
    }

    fun getEmulatorCore(): EmulatorCore = emulatorCore

    private fun loadAndStartROM(romPath: String) {
        lifecycleScope.launch {
            try {
                // ROM読み込み
                if (emulatorCore.loadROM(romPath)) {
                    // エミュレーション開始
                    if (emulatorCore.start()) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.emulation_started),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.emulation_failed), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.rom_load_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "${getString(R.string.error_prefix)}${e.message}", Toast.LENGTH_SHORT).show()
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
            btnPause.text = if (isPaused) getString(R.string.btn_resume) else getString(R.string.btn_pause)

            if (isPaused) {
                emulatorCore.resume()
                btnPause.text = getString(R.string.btn_pause)
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
                    Toast.makeText(this@MainActivity, getString(R.string.game_saved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "${getString(R.string.error_prefix)}${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGame() {
        lifecycleScope.launch {
            try {
                val saveDir = getExternalFilesDir("saves")
                val savePath = "$saveDir/save_slot_1.sav"

                if (emulatorCore.loadState(savePath)) {
                    Toast.makeText(this@MainActivity, getString(R.string.game_loaded), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.load_failed), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "${getString(R.string.error_prefix)}${e.message}", Toast.LENGTH_SHORT).show()
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
