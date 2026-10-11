// app/src/main/java/com/mgba/emulator/CheatsFragment.kt

package com.mgba.emulator

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/**
 * Cheats fragment for GameShark code management
 */
class CheatsFragment : Fragment() {
    private lateinit var emulatorCore: EmulatorCore
    private val cheatList = mutableListOf<CheatInfo>()
    private var cheatAdapter: CheatListAdapter? = null

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
            text = getString(R.string.cheats)
            textSize = 24f
            setPadding(16, 16, 16, 0)
        })

        // Description
        addView(TextView(requireContext()).apply {
            text = getString(R.string.cheats_desc)
            textSize = 14f
            setTextColor(0xFF757575.toInt())
            setPadding(16, 8, 16, 16)
        })

        // Input section
        addView(createInputSection())

        // RecyclerView for cheat list
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            layoutManager = LinearLayoutManager(requireContext())
            cheatAdapter = CheatListAdapter(cheatList, requireContext()) { cheatInfo, action ->
                handleCheatAction(cheatInfo, action)
            }
            adapter = cheatAdapter
        }
        addView(recyclerView)

        // Load cheats
        loadCheats()
    }

    private fun createInputSection(): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 8, 16, 8)
            setBackgroundColor(0xFFF5F5F5.toInt())

            // Code input
            val codeInput = EditText(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                hint = getString(R.string.cheat_code_hint)
                inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
            addView(codeInput)

            // Add button
            addView(Button(requireContext()).apply {
                text = getString(R.string.add_cheat)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    val code = codeInput.text.toString().trim()
                    if (code.isNotEmpty()) {
                        lifecycleScope.launch {
                            val result = emulatorCore.addCheat(code)
                            if (result) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.cheat_added),
                                    Toast.LENGTH_SHORT
                                ).show()
                                codeInput.text.clear()
                                loadCheats()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.cheat_add_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            })
        }
    }

    private fun loadCheats() {
        lifecycleScope.launch {
            cheatList.clear()
            val count = emulatorCore.getCheatCount()
            for (i in 0 until count) {
                val code = emulatorCore.getCheatCode(i)
                if (code != null) {
                    cheatList.add(CheatInfo(
                        index = i,
                        code = code,
                        enabled = true
                    ))
                }
            }
            cheatAdapter?.notifyDataSetChanged()
        }
    }

    private fun handleCheatAction(cheatInfo: CheatInfo, action: String) {
        lifecycleScope.launch {
            when (action) {
                "toggle" -> {
                    val newState = !cheatInfo.enabled
                    emulatorCore.enableCheat(cheatInfo.index, newState)
                    cheatInfo.enabled = newState
                    cheatAdapter?.notifyDataSetChanged()
                }
                "remove" -> {
                    emulatorCore.removeCheat(cheatInfo.index)
                    loadCheats()
                }
            }
        }
    }

    companion object {
        fun newInstance() = CheatsFragment()
    }
}

/**
 * Cheat info data class
 */
data class CheatInfo(
    val index: Int,
    val code: String,
    var enabled: Boolean
)

/**
 * RecyclerView adapter for cheat list
 */
class CheatListAdapter(
    private val cheats: List<CheatInfo>,
    private val context: android.content.Context,
    private val onAction: (CheatInfo, String) -> Unit
) : RecyclerView.Adapter<CheatListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val codeView: TextView = itemView.findViewById(android.R.id.text1)
        private val statusView: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(cheat: CheatInfo) {
            codeView.text = cheat.code
            statusView.text = if (cheat.enabled) "✓ Enabled" else "✗ Disabled"

            itemView.setOnLongClickListener {
                onAction(cheat, "remove")
                true
            }

            itemView.setOnClickListener {
                onAction(cheat, "toggle")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cheats[position])
    }

    override fun getItemCount(): Int = cheats.size
}
