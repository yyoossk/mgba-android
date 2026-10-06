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
                Log.e(TAG, "Failed to load native library", e)
                throw RuntimeException("Failed to load mGBA native library", e)
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
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed", e)
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
        } catch (e: Exception) {
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
