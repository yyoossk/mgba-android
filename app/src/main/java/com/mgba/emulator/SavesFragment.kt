// app/src/main/java/com/mgba/emulator/SavesFragment.kt

package com.mgba.emulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Saves fragment for managing save states across multiple slots
 */
class SavesFragment : Fragment() {
    private lateinit var emulatorCore: EmulatorCore
    private val saveSlots = mutableListOf<SaveSlotInfo>()

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

        emulatorCore = (activity as? MainActivity)?.getEmulatorCore() ?: return@apply

        // Title
        addView(TextView(requireContext()).apply {
            text = getString(R.string.save_states)
            textSize = 24f
            setPadding(16, 16, 16, 0)
        })

        // Description
        addView(TextView(requireContext()).apply {
            text = getString(R.string.save_states_desc)
            textSize = 14f
            setTextColor(0xFF757575.toInt())
            setPadding(16, 8, 16, 16)
        })

        // Save slots
        for (slot in 0 until MAX_SAVE_SLOTS) {
            addView(createSaveSlotView(slot))
        }

        // Load save info
        loadSaveSlots()
    }

    private fun createSaveSlotView(slot: Int): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)

            // Slot info
            val infoLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )

                addView(TextView(requireContext()).apply {
                    text = "${getString(R.string.slot)} $slot"
                    textSize = 16f
                    setTextColor(0xFF000000.toInt())
                })

                addView(TextView(requireContext()).apply {
                    tag = "slot_info_$slot"
                    text = getString(R.string.save_slot_empty)
                    textSize = 14f
                    setTextColor(0xFF757575.toInt())
                    setPadding(0, 4, 0, 0)
                })
            }
            addView(infoLayout)

            // Buttons
            val buttonLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                addView(Button(requireContext()).apply {
                    text = getString(R.string.btn_save)
                    textSize = 12f
                    setPadding(8, 4, 8, 4)
                    setOnClickListener {
                        saveToSlot(slot)
                    }
                })

                addView(Button(requireContext()).apply {
                    text = getString(R.string.btn_load)
                    textSize = 12f
                    setPadding(8, 4, 8, 4)
                    setOnClickListener {
                        loadFromSlot(slot)
                    }
                })

                addView(Button(requireContext()).apply {
                    text = getString(R.string.delete)
                    textSize = 12f
                    setPadding(8, 4, 8, 4)
                    setOnClickListener {
                        deleteSlot(slot)
                    }
                })
            }
            addView(buttonLayout)
        }
    }

    private fun loadSaveSlots() {
        lifecycleScope.launch {
            saveSlots.clear()
            val saveDir = requireActivity().getExternalFilesDir("saves")

            for (slot in 0 until MAX_SAVE_SLOTS) {
                val savePath = "$saveDir/save_slot_$slot.sav"
                val file = File(savePath)

                if (file.exists()) {
                    val lastModified = file.lastModified()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(lastModified))
                    val sizeStr = "${file.length() / 1024} KB"

                    saveSlots.add(SaveSlotInfo(slot, dateStr, sizeStr, true))
                    updateSlotInfo(slot, "$dateStr - $sizeStr")
                } else {
                    saveSlots.add(SaveSlotInfo(slot, "", "", false))
                }
            }
        }
    }

    private fun updateSlotInfo(slot: Int, info: String) {
        view?.findViewWithTag<TextView>("slot_info_$slot")?.text = info
    }

    private fun saveToSlot(slot: Int) {
        lifecycleScope.launch {
            try {
                val saveDir = requireActivity().getExternalFilesDir("saves")
                saveDir?.mkdirs()
                val savePath = "$saveDir/save_slot_$slot.sav"

                if (emulatorCore.saveStateSlot(slot)) {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.save_slot_saved)} $slot",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadSaveSlots()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.save_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "${getString(R.string.error_prefix)}${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadFromSlot(slot: Int) {
        lifecycleScope.launch {
            try {
                if (emulatorCore.loadStateSlot(slot)) {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.save_slot_loaded)} $slot",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.load_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "${getString(R.string.error_prefix)}${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteSlot(slot: Int) {
        lifecycleScope.launch {
            try {
                val saveDir = requireActivity().getExternalFilesDir("saves")
                val savePath = "$saveDir/save_slot_$slot.sav"
                val file = File(savePath)

                if (file.exists() && file.delete()) {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.save_slot_deleted)} $slot",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadSaveSlots()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.delete_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "${getString(R.string.error_prefix)}${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val MAX_SAVE_SLOTS = 10

        fun newInstance() = SavesFragment()
    }
}

/**
 * Save slot info data class
 */
data class SaveSlotInfo(
    val slot: Int,
    val lastModified: String,
    val size: String,
    val exists: Boolean
)
