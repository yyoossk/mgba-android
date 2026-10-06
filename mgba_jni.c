// app/src/main/jni/mgba_jni.c
// JNI実装：JavaとネイティブmGBAコアの連携

#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <pthread.h>
#include <stdint.h>

#define LOG_TAG "mGBA-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// ========== mGBA コアの構造体（仮定）==========
// 実際のmGBAソースから以下の定義が必要：
// struct mCore
// struct mCoreThread
// enum mLogLevel

typedef struct {
    void *core;                    // mCoreインスタンス
    void *thread;                  // エミュレータスレッド
    void *renderer;                // フレームバッファレンダラー
    int is_running;
    int frame_width;
    int frame_height;
    uint32_t *frame_buffer;        // RGB565フレームバッファ
    pthread_mutex_t lock;
} EmulatorState;

static EmulatorState emulator = {0};

// ========== ネイティブメソッド実装 ==========

/**
 * nativeInitialize()
 * エミュレータの初期化
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeInitialize(JNIEnv *env, jobject obj) {
    LOGI("Initializing mGBA emulator");

    pthread_mutex_init(&emulator.lock, NULL);
    emulator.is_running = 0;
    emulator.frame_width = 240;
    emulator.frame_height = 160;

    // フレームバッファ確保（GBA解像度: 240x160 RGB565 = 76,800 bytes）
    emulator.frame_buffer = (uint32_t *)malloc(
        emulator.frame_width * emulator.frame_height * 2
    );

    if (!emulator.frame_buffer) {
        LOGE("Failed to allocate frame buffer");
        return -1;
    }

    LOGI("Emulator initialized successfully");
    return 0;
}

/**
 * nativeLoadROM(String romPath)
 * ROMファイルをロード
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeLoadROM(
    JNIEnv *env, jobject obj, jstring rom_path) {

    const char *path = (*env)->GetStringUTFChars(env, rom_path, NULL);
    LOGI("Loading ROM: %s", path);

    // TODO: mGBA コアのROMロード処理
    //例: mCoreLoadFile(emulator.core, path);

    (*env)->ReleaseStringUTFChars(env, rom_path, path);
    return 0;
}

/**
 * nativeStart()
 * エミュレーション開始
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeStart(JNIEnv *env, jobject obj) {
    LOGI("Starting emulation");

    if (emulator.is_running) {
        LOGI("Emulator already running");
        return 0;
    }

    pthread_mutex_lock(&emulator.lock);
    emulator.is_running = 1;
    pthread_mutex_unlock(&emulator.lock);

    // TODO: エミュレータスレッド開始
    // pthread_create(&emulator.thread, NULL, emulator_thread_func, NULL);

    return 0;
}

/**
 * nativePause()
 * エミュレーション一時停止
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativePause(JNIEnv *env, jobject obj) {
    LOGI("Pausing emulation");

    pthread_mutex_lock(&emulator.lock);
    emulator.is_running = 0;
    pthread_mutex_unlock(&emulator.lock);

    return 0;
}

/**
 * nativeResume()
 * エミュレーション再開
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeResume(JNIEnv *env, jobject obj) {
    LOGI("Resuming emulation");

    pthread_mutex_lock(&emulator.lock);
    emulator.is_running = 1;
    pthread_mutex_unlock(&emulator.lock);

    return 0;
}

/**
 * nativeStop()
 * エミュレーション停止
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeStop(JNIEnv *env, jobject obj) {
    LOGI("Stopping emulation");

    pthread_mutex_lock(&emulator.lock);
    emulator.is_running = 0;
    pthread_mutex_unlock(&emulator.lock);

    // スレッド終了待機
    // pthread_join(emulator.thread, NULL);

    return 0;
}

/**
 * nativeSetKeyState(int keyCode, boolean isPressed)
 * キー入力設定
 */
JNIEXPORT void JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeSetKeyState(
    JNIEnv *env, jobject obj, jint key_code, jboolean is_pressed) {

    // GBA キーマッピング
    // Key A, B, Select, Start, Right, Left, Up, Down, R, L

    LOGI("Key input: code=%d pressed=%d", key_code, is_pressed);

    // TODO: mGBA コアへのキー状態通知
    // mCoreInputSetState(emulator.core, key_code, is_pressed);
}

/**
 * nativeGetFrameBuffer()
 * フレームバッファへのポインタを取得
 */
JNIEXPORT jlong JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeGetFrameBuffer(
    JNIEnv *env, jobject obj) {

    return (jlong)(intptr_t)emulator.frame_buffer;
}

/**
 * nativeGetFrameWidth()
 * フレーム幅を取得
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeGetFrameWidth(JNIEnv *env, jobject obj) {
    return emulator.frame_width;
}

/**
 * nativeGetFrameHeight()
 * フレーム高さを取得
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeGetFrameHeight(JNIEnv *env, jobject obj) {
    return emulator.frame_height;
}

/**
 * nativeSaveState(String path)
 * セーブステート作成
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeSaveState(
    JNIEnv *env, jobject obj, jstring save_path) {

    const char *path = (*env)->GetStringUTFChars(env, save_path, NULL);
    LOGI("Saving state to: %s", path);

    // TODO: mGBA セーブ処理
    // mCoreSaveStateNamed(emulator.core, path, SAVESTATE_SCREENSHOT);

    (*env)->ReleaseStringUTFChars(env, save_path, path);
    return 0;
}

/**
 * nativeLoadState(String path)
 * セーブステート読み込み
 */
JNIEXPORT jint JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeLoadState(
    JNIEnv *env, jobject obj, jstring save_path) {

    const char *path = (*env)->GetStringUTFChars(env, save_path, NULL);
    LOGI("Loading state from: %s", path);

    // TODO: mGBA 読み込み処理
    // mCoreLoadStateNamed(emulator.core, path);

    (*env)->ReleaseStringUTFChars(env, save_path, path);
    return 0;
}

/**
 * nativeDestroy()
 * エミュレータクリーンアップ
 */
JNIEXPORT void JNICALL
Java_com_mgba_emulator_EmulatorCore_nativeDestroy(JNIEnv *env, jobject obj) {
    LOGI("Destroying emulator");

    // 実行停止
    emulator.is_running = 0;

    // メモリ解放
    if (emulator.frame_buffer) {
        free(emulator.frame_buffer);
        emulator.frame_buffer = NULL;
    }

    pthread_mutex_destroy(&emulator.lock);
}
