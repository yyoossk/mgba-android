// app/src/main/java/com/mgba/emulator/PreferencesManager.kt

package com.mgba.emulator

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "mgba_preferences"
private val Context.dataStore by preferencesDataStore(name = PREFERENCES_NAME)

/**
 * Manages persistent preferences for emulator settings
 */
class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        // Video Settings
        private val SHADER_TYPE = intPreferencesKey("shader_type")
        private val ASPECT_RATIO = floatPreferencesKey("aspect_ratio")
        private val FRAME_SKIP = intPreferencesKey("frame_skip")
        private val BACKGROUND_COLOR = intPreferencesKey("background_color")

        // Audio Settings
        private val MASTER_VOLUME = intPreferencesKey("master_volume")
        private val CHANNEL_VOLUME_A = intPreferencesKey("channel_volume_a")
        private val CHANNEL_VOLUME_B = intPreferencesKey("channel_volume_b")
        private val SAMPLE_RATE = intPreferencesKey("sample_rate")
        private val HIGH_PASS_FILTER = booleanPreferencesKey("high_pass_filter")

        // Speed Control
        private val FAST_FORWARD_ENABLED = booleanPreferencesKey("fast_forward_enabled")
        private val SLOW_MOTION_SPEED = floatPreferencesKey("slow_motion_speed")

        // Input Settings
        private val BUTTON_LAYOUT = intPreferencesKey("button_layout")
        private val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")

        // Default values
        const val DEFAULT_SHADER_TYPE = 0
        const val DEFAULT_ASPECT_RATIO = 1.0f
        const val DEFAULT_FRAME_SKIP = 0
        const val DEFAULT_BACKGROUND_COLOR = 0xFF000000.toInt()
        const val DEFAULT_MASTER_VOLUME = 100
        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_BUTTON_LAYOUT = 0
    }

    // ========== Video Settings ==========

    fun getShaderType(): Flow<Int> = dataStore.data.map { it[SHADER_TYPE] ?: DEFAULT_SHADER_TYPE }

    suspend fun setShaderType(value: Int) {
        dataStore.edit { it[SHADER_TYPE] = value }
    }

    fun getAspectRatio(): Flow<Float> = dataStore.data.map { it[ASPECT_RATIO] ?: DEFAULT_ASPECT_RATIO }

    suspend fun setAspectRatio(value: Float) {
        dataStore.edit { it[ASPECT_RATIO] = value }
    }

    fun getFrameSkip(): Flow<Int> = dataStore.data.map { it[FRAME_SKIP] ?: DEFAULT_FRAME_SKIP }

    suspend fun setFrameSkip(value: Int) {
        dataStore.edit { it[FRAME_SKIP] = value }
    }

    fun getBackgroundColor(): Flow<Int> = dataStore.data.map { it[BACKGROUND_COLOR] ?: DEFAULT_BACKGROUND_COLOR }

    suspend fun setBackgroundColor(value: Int) {
        dataStore.edit { it[BACKGROUND_COLOR] = value }
    }

    // ========== Audio Settings ==========

    fun getMasterVolume(): Flow<Int> = dataStore.data.map { it[MASTER_VOLUME] ?: DEFAULT_MASTER_VOLUME }

    suspend fun setMasterVolume(value: Int) {
        dataStore.edit { it[MASTER_VOLUME] = value }
    }

    fun getChannelVolumeA(): Flow<Int> = dataStore.data.map { it[CHANNEL_VOLUME_A] ?: DEFAULT_MASTER_VOLUME }

    suspend fun setChannelVolumeA(value: Int) {
        dataStore.edit { it[CHANNEL_VOLUME_A] = value }
    }

    fun getChannelVolumeB(): Flow<Int> = dataStore.data.map { it[CHANNEL_VOLUME_B] ?: DEFAULT_MASTER_VOLUME }

    suspend fun setChannelVolumeB(value: Int) {
        dataStore.edit { it[CHANNEL_VOLUME_B] = value }
    }

    fun getSampleRate(): Flow<Int> = dataStore.data.map { it[SAMPLE_RATE] ?: DEFAULT_SAMPLE_RATE }

    suspend fun setSampleRate(value: Int) {
        dataStore.edit { it[SAMPLE_RATE] = value }
    }

    fun getHighPassFilter(): Flow<Boolean> = dataStore.data.map { it[HIGH_PASS_FILTER] ?: false }

    suspend fun setHighPassFilter(enabled: Boolean) {
        dataStore.edit { it[HIGH_PASS_FILTER] = enabled }
    }

    // ========== Speed Control ==========

    fun getFastForwardEnabled(): Flow<Boolean> = dataStore.data.map { it[FAST_FORWARD_ENABLED] ?: false }

    suspend fun setFastForwardEnabled(enabled: Boolean) {
        dataStore.edit { it[FAST_FORWARD_ENABLED] = enabled }
    }

    fun getSlowMotionSpeed(): Flow<Float> = dataStore.data.map { it[SLOW_MOTION_SPEED] ?: 0.5f }

    suspend fun setSlowMotionSpeed(speed: Float) {
        dataStore.edit { it[SLOW_MOTION_SPEED] = speed }
    }

    // ========== Input Settings ==========

    fun getButtonLayout(): Flow<Int> = dataStore.data.map { it[BUTTON_LAYOUT] ?: DEFAULT_BUTTON_LAYOUT }

    suspend fun setButtonLayout(value: Int) {
        dataStore.edit { it[BUTTON_LAYOUT] = value }
    }

    fun getHapticFeedback(): Flow<Boolean> = dataStore.data.map { it[HAPTIC_FEEDBACK] ?: true }

    suspend fun setHapticFeedback(enabled: Boolean) {
        dataStore.edit { it[HAPTIC_FEEDBACK] = enabled }
    }
}
