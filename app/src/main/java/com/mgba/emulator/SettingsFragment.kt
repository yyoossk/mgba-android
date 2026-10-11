// app/src/main/java/com/mgba/emulator/SettingsFragment.kt

package com.mgba.emulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Settings fragment for video, audio, and input configuration
 */
class SettingsFragment : Fragment() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = LinearLayout(requireContext()).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        preferencesManager = PreferencesManager(requireContext())

        // Title
        addView(TextView(requireContext()).apply {
            text = getString(R.string.menu_settings)
            textSize = 24f
            setPadding(16, 16, 16, 0)
        })

        // ===== Video Settings =====
        addView(createSectionHeader(getString(R.string.video_settings)))

        // Aspect Ratio
        addView(createSetting(
            title = getString(R.string.aspect_ratio),
            description = getString(R.string.aspect_ratio_desc),
            value = "1.0x"
        ))

        // Frame Skip
        val frameSkipSeekBar = createSeekBarSetting(
            title = getString(R.string.frame_skip),
            maxValue = 9
        )
        addView(frameSkipSeekBar)

        // Shader
        addView(createSetting(
            title = getString(R.string.shader),
            description = getString(R.string.shader_default)
        ))

        // ===== Audio Settings =====
        addView(createSectionHeader(getString(R.string.audio_settings)))

        // Master Volume
        val volumeSeekBar = createSeekBarSetting(
            title = getString(R.string.master_volume),
            maxValue = 100
        )
        addView(volumeSeekBar)

        // Sample Rate
        addView(createSetting(
            title = getString(R.string.sample_rate),
            description = "44100 Hz"
        ))

        // High Pass Filter
        addView(createToggleSetting(
            title = getString(R.string.high_pass_filter),
            description = getString(R.string.high_pass_filter_desc),
            onToggle = { enabled ->
                lifecycleScope.launch {
                    preferencesManager.setHighPassFilter(enabled)
                }
            }
        ))

        // ===== Input Settings =====
        addView(createSectionHeader(getString(R.string.input_settings)))

        // Haptic Feedback
        addView(createToggleSetting(
            title = getString(R.string.haptic_feedback),
            description = getString(R.string.haptic_feedback_desc),
            onToggle = { enabled ->
                lifecycleScope.launch {
                    preferencesManager.setHapticFeedback(enabled)
                }
            }
        ))
    }

    private fun createSectionHeader(title: String): View {
        return TextView(requireContext()).apply {
            text = title
            textSize = 18f
            setTextColor(0xFF1976D2.toInt())
            setPadding(16, 24, 16, 8)
        }
    }

    private fun createSetting(
        title: String,
        description: String = "",
        value: String = ""
    ): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)

            addView(TextView(requireContext()).apply {
                text = title
                textSize = 16f
                setTextColor(0xFF000000.toInt())
            })

            if (description.isNotEmpty()) {
                addView(TextView(requireContext()).apply {
                    text = if (value.isNotEmpty()) "$description - $value" else description
                    textSize = 14f
                    setTextColor(0xFF757575.toInt())
                    setPadding(0, 4, 0, 0)
                })
            }
        }
    }

    private fun createSeekBarSetting(
        title: String,
        maxValue: Int = 100
    ): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)

            val titleView = TextView(requireContext()).apply {
                text = title
                textSize = 16f
                setTextColor(0xFF000000.toInt())
            }
            addView(titleView)

            val seekBar = SeekBar(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                max = maxValue
                progress = maxValue / 2
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        titleView.text = "$title: $progress"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }
            addView(seekBar)
        }
    }

    private fun createToggleSetting(
        title: String,
        description: String = "",
        onToggle: (Boolean) -> Unit
    ): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)

            val contentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )

                addView(TextView(requireContext()).apply {
                    text = title
                    textSize = 16f
                    setTextColor(0xFF000000.toInt())
                })

                if (description.isNotEmpty()) {
                    addView(TextView(requireContext()).apply {
                        text = description
                        textSize = 14f
                        setTextColor(0xFF757575.toInt())
                        setPadding(0, 4, 0, 0)
                    })
                }
            }
            addView(contentLayout)

            val switch = Switch(requireContext()).apply {
                isChecked = true
                setOnCheckedChangeListener { _, isChecked ->
                    onToggle(isChecked)
                }
            }
            addView(switch)
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
