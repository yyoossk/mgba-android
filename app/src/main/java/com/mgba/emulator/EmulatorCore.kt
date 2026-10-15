// app/src/main/java/com/mgba/emulator/EmulatorCore.kt

package com.mgba.emulator

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * mGBA エミュレータコアのJavaラッパー
 * ネイティブコード(JNI)とのインターフェース
 */
class EmulatorCore {
    companion object {
        private const val TAG = "EmulatorCore"

        init {
            // ネイティブライブラリのロード
            try {
                System.loadLibrary("mgba-core")
                Log.i(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native library not available - running in stub mode", e)
                // Continue anyway - we'll use stub implementations
            }
        }
    }

    private var initialized = false

    // ========== ネイティブメソッド宣言 ==========

    private external fun nativeInitialize(): Int
    private external fun nativeLoadROM(romPath: String): Int
    private external fun nativeStart(): Int
    private external fun nativePause(): Int
    private external fun nativeResume(): Int
    private external fun nativeStop(): Int
    private external fun nativeSetKeyState(keyCode: Int, isPressed: Boolean)
    private external fun nativeGetFrameBuffer(): Long
    private external fun nativeGetFrameWidth(): Int
    private external fun nativeGetFrameHeight(): Int
    private external fun nativeSaveState(savePath: String): Int
    private external fun nativeLoadState(savePath: String): Int
    private external fun nativeDestroy()

    // ========== ビデオ設定 ==========
    private external fun nativeSetShader(shaderType: Int): Int
    private external fun nativeSetAspectRatio(ratio: Float): Int
    private external fun nativeSetFrameSkip(skip: Int): Int
    private external fun nativeSetBackgroundColor(color: Int): Int
    private external fun nativeTakeScreenshot(outputPath: String): Int

    // ========== オーディオ設定 ==========
    private external fun nativeSetMasterVolume(volume: Int): Int
    private external fun nativeSetChannelVolume(channel: Int, volume: Int): Int
    private external fun nativeSetSampleRate(sampleRate: Int): Int
    private external fun nativeSetHighPassFilter(enabled: Boolean): Int

    // ========== 高速化機能 ==========
    private external fun nativeSetFastForward(enabled: Boolean): Int
    private external fun nativeSetSlowMotion(speed: Float): Int

    // ========== チート機能 ==========
    private external fun nativeAddCheat(code: String): Int
    private external fun nativeRemoveCheat(index: Int): Int
    private external fun nativeEnableCheat(index: Int, enabled: Boolean): Int
    private external fun nativeGetCheatCount(): Int
    private external fun nativeGetCheatCode(index: Int): String?

    // ========== セーブスロット管理 ==========
    private external fun nativeSaveStateSlot(slot: Int): Int
    private external fun nativeLoadStateSlot(slot: Int): Int
    private external fun nativeDeleteStateSlot(slot: Int): Int

    // ========== パブリックインターフェース ==========

    /**
     * エミュレータ初期化
     */
    fun initialize(): Boolean {
        return try {
            val result = nativeInitialize()
            initialized = (result == 0)
            Log.i(TAG, "Emulator initialized: $initialized")
            initialized
        } catch (e: Throwable) {
            // UnsatisfiedLinkError はExceptionではないため Throwable で捕捉する
            Log.e(TAG, "Initialization failed (native core missing?)", e)
            false
        }
    }

    /**
     * ROMファイルをロード
     */
    suspend fun loadROM(romPath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeLoadROM(romPath)
            Log.i(TAG, "ROM loaded: $romPath (result=$result)")
            result == 0
        } catch (e: Throwable) {
            Log.e(TAG, "ROM loading failed: $romPath", e)
            false
        }
    }

    /**
     * エミュレーション開始
     */
    fun start(): Boolean {
        return try {
            val result = nativeStart()
            Log.i(TAG, "Emulation started")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Start failed", e)
            false
        }
    }

    /**
     * エミュレーション一時停止
     */
    fun pause(): Boolean {
        return try {
            val result = nativePause()
            Log.i(TAG, "Emulation paused")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Pause failed", e)
            false
        }
    }

    /**
     * エミュレーション再開
     */
    fun resume(): Boolean {
        return try {
            val result = nativeResume()
            Log.i(TAG, "Emulation resumed")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Resume failed", e)
            false
        }
    }

    /**
     * エミュレーション停止
     */
    fun stop(): Boolean {
        return try {
            val result = nativeStop()
            Log.i(TAG, "Emulation stopped")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Stop failed", e)
            false
        }
    }

    /**
     * キー入力を設定
     *
     * @param keyCode GBAキーコード（GBAKey enum参照）
     * @param isPressed キーが押されているか
     */
    fun setKeyState(keyCode: GBAKey, isPressed: Boolean) {
        try {
            nativeSetKeyState(keyCode.value, isPressed)
        } catch (e: Exception) {
            Log.e(TAG, "Key state set failed", e)
        }
    }

    /**
     * フレームバッファの情報を取得
     */
    fun getFrameBufferInfo(): FrameBufferInfo {
        return try {
            FrameBufferInfo(
                bufferPtr = nativeGetFrameBuffer(),
                width = nativeGetFrameWidth(),
                height = nativeGetFrameHeight()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get frame buffer info", e)
            FrameBufferInfo(0, 240, 160)
        }
    }

    /**
     * ゲームステートをセーブ
     */
    suspend fun saveState(savePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeSaveState(savePath)
            Log.i(TAG, "State saved: $savePath")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Save state failed", e)
            false
        }
    }

    /**
     * ゲームステートを読み込み
     */
    suspend fun loadState(savePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeLoadState(savePath)
            Log.i(TAG, "State loaded: $savePath")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Load state failed", e)
            false
        }
    }

    /**
     * クリーンアップ・メモリ解放
     */
    fun destroy() {
        try {
            nativeDestroy()
            initialized = false
            Log.i(TAG, "Emulator destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Destroy failed", e)
        }
    }

    // ========== ビデオ設定実装 ==========

    fun setShader(shaderType: Int): Boolean {
        return try {
            val result = nativeSetShader(shaderType)
            Log.i(TAG, "Shader set to: $shaderType")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set shader failed", e)
            false
        }
    }

    fun setAspectRatio(ratio: Float): Boolean {
        return try {
            val result = nativeSetAspectRatio(ratio)
            Log.i(TAG, "Aspect ratio set to: $ratio")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set aspect ratio failed", e)
            false
        }
    }

    fun setFrameSkip(skip: Int): Boolean {
        return try {
            val result = nativeSetFrameSkip(skip)
            Log.i(TAG, "Frame skip set to: $skip")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set frame skip failed", e)
            false
        }
    }

    fun setBackgroundColor(color: Int): Boolean {
        return try {
            val result = nativeSetBackgroundColor(color)
            Log.i(TAG, "Background color set")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set background color failed", e)
            false
        }
    }

    fun takeScreenshot(outputPath: String): Boolean {
        return try {
            val result = nativeTakeScreenshot(outputPath)
            Log.i(TAG, "Screenshot saved: $outputPath")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Take screenshot failed", e)
            false
        }
    }

    // ========== オーディオ設定実装 ==========

    fun setMasterVolume(volume: Int): Boolean {
        return try {
            val result = nativeSetMasterVolume(volume)
            Log.i(TAG, "Master volume set to: $volume")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set master volume failed", e)
            false
        }
    }

    fun setChannelVolume(channel: Int, volume: Int): Boolean {
        return try {
            val result = nativeSetChannelVolume(channel, volume)
            Log.i(TAG, "Channel $channel volume set to: $volume")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set channel volume failed", e)
            false
        }
    }

    fun setSampleRate(sampleRate: Int): Boolean {
        return try {
            val result = nativeSetSampleRate(sampleRate)
            Log.i(TAG, "Sample rate set to: $sampleRate")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set sample rate failed", e)
            false
        }
    }

    fun setHighPassFilter(enabled: Boolean): Boolean {
        return try {
            val result = nativeSetHighPassFilter(enabled)
            Log.i(TAG, "High pass filter set to: $enabled")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set high pass filter failed", e)
            false
        }
    }

    // ========== 高速化機能実装 ==========

    fun setFastForward(enabled: Boolean): Boolean {
        return try {
            val result = nativeSetFastForward(enabled)
            Log.i(TAG, "Fast forward: $enabled")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set fast forward failed", e)
            false
        }
    }

    fun setSlowMotion(speed: Float): Boolean {
        return try {
            val result = nativeSetSlowMotion(speed)
            Log.i(TAG, "Slow motion speed: $speed")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Set slow motion failed", e)
            false
        }
    }

    // ========== チート機能実装 ==========

    suspend fun addCheat(code: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeAddCheat(code)
            Log.i(TAG, "Cheat added: $code")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Add cheat failed", e)
            false
        }
    }

    suspend fun removeCheat(index: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeRemoveCheat(index)
            Log.i(TAG, "Cheat removed: $index")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Remove cheat failed", e)
            false
        }
    }

    suspend fun enableCheat(index: Int, enabled: Boolean): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeEnableCheat(index, enabled)
            Log.i(TAG, "Cheat $index enabled: $enabled")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Enable cheat failed", e)
            false
        }
    }

    suspend fun getCheatCount(): Int = withContext(Dispatchers.IO) {
        return@withContext try {
            nativeGetCheatCount()
        } catch (e: Exception) {
            Log.e(TAG, "Get cheat count failed", e)
            0
        }
    }

    suspend fun getCheatCode(index: Int): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            nativeGetCheatCode(index)
        } catch (e: Exception) {
            Log.e(TAG, "Get cheat code failed", e)
            null
        }
    }

    // ========== セーブスロット管理実装 ==========

    suspend fun saveStateSlot(slot: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeSaveStateSlot(slot)
            Log.i(TAG, "State saved to slot: $slot")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Save state slot failed", e)
            false
        }
    }

    suspend fun loadStateSlot(slot: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeLoadStateSlot(slot)
            Log.i(TAG, "State loaded from slot: $slot")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Load state slot failed", e)
            false
        }
    }

    suspend fun deleteStateSlot(slot: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val result = nativeDeleteStateSlot(slot)
            Log.i(TAG, "State slot deleted: $slot")
            result == 0
        } catch (e: Exception) {
            Log.e(TAG, "Delete state slot failed", e)
            false
        }
    }
}

// ========== データクラス・Enum ==========

/**
 * フレームバッファ情報
 */
data class FrameBufferInfo(
    val bufferPtr: Long,   // ネイティブメモリへのポインタ
    val width: Int,        // フレーム幅（通常240）
    val height: Int        // フレーム高さ（通常160）
)

/**
 * GBAキーコード定義
 */
enum class GBAKey(val value: Int) {
    A(0),
    B(1),
    SELECT(2),
    START(3),
    RIGHT(4),
    LEFT(5),
    UP(6),
    DOWN(7),
    R(8),
    L(9)
}
