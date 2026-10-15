package com.mgba.emulator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Game loader activity
 */
class GameLoaderActivity : Activity() {
    companion object {
        private const val TAG = "GameLoaderActivity"
    }

    private val gameList = mutableListOf<GameInfo>()
    private var gameAdapter: GameListAdapter? = null
    private val requestRomPick = 1001

    private fun openRomPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, requestRomPick)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != requestRomPick || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            val name = contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && idx >= 0) c.getString(idx) else null
            } ?: "game_${System.currentTimeMillis()}.gba"

            val romDir = getExternalFilesDir("roms") ?: throw IllegalStateException("roms dir unavailable")
            romDir.mkdirs()
            val dest = java.io.File(romDir, name)
            contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IllegalStateException("cannot open input stream")

            loadGameList()
            gameAdapter?.notifyDataSetChanged()
            Toast.makeText(this, getString(R.string.rom_added), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "ROM import failed", e)
            Toast.makeText(this, getString(R.string.error_prefix) + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "GameLoaderActivity.onCreate() called")

        try {
            // Create main container
            val mainContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            // Create button
            val btnSelectFile = Button(this).apply {
                text = getString(R.string.btn_select_game)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    Log.i(TAG, "Select game button clicked")
                    openRomPicker()
                }
            }

            // Create RecyclerView
            val recyclerView = RecyclerView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                layoutManager = LinearLayoutManager(this@GameLoaderActivity)
            }

            // Setup adapter
            val gameAdapter = GameListAdapter(gameList, this) { gameInfo ->
                launchGame(gameInfo.path)
            }
            recyclerView.adapter = gameAdapter
            this.gameAdapter = gameAdapter

            // Add views
            mainContainer.addView(btnSelectFile)
            mainContainer.addView(recyclerView)

            setContentView(mainContainer)

            // Load games
            loadGameList()
            gameAdapter.notifyDataSetChanged()

            Log.i(TAG, "GameLoaderActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in GameLoaderActivity: ${e.message}", e)
            e.printStackTrace()

            val errorText = TextView(this).apply {
                text = "Error: ${e.message}"
                textSize = 18f
            }
            setContentView(errorText)
        }
    }

    private fun loadGameList() {
        try {
            val romDir = getExternalFilesDir("roms")
            romDir?.mkdirs()

            gameList.clear()
            romDir?.listFiles()?.filter {
                it.isFile && (it.extension == "gba" || it.extension == "zip")
            }?.forEach { file ->
                gameList.add(GameInfo(
                    name = file.nameWithoutExtension,
                    path = file.absolutePath,
                    size = file.length()
                ))
            }

            Log.i(TAG, "Loaded ${gameList.size} games")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game list: ${e.message}", e)
        }
    }

    private fun launchGame(romPath: String) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("ROM_PATH", romPath)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching game: ${e.message}", e)
            val errorMsg = getString(R.string.error_prefix) + e.message
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Game info
 */
data class GameInfo(
    val name: String,
    val path: String,
    val size: Long
)

/**
 * RecyclerView adapter
 */
class GameListAdapter(
    private val games: List<GameInfo>,
    private val context: android.content.Context,
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<GameListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(android.R.id.text1)
        private val sizeView: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(game: GameInfo) {
            titleView.text = game.name
            val mbSuffix = context.getString(R.string.toast_file_size_mb)
            sizeView.text = "${game.size / 1024 / 1024}$mbSuffix"
            itemView.setOnClickListener {
                onGameClick(game)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size
}
