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
                text = "Select Game ROM"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    Log.i(TAG, "Select game button clicked")
                    Toast.makeText(
                        this@GameLoaderActivity,
                        "ROM selection coming soon",
                        Toast.LENGTH_SHORT
                    ).show()
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
            val gameAdapter = GameListAdapter(gameList) { gameInfo ->
                launchGame(gameInfo.path)
            }
            recyclerView.adapter = gameAdapter

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
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<GameListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(android.R.id.text1)
        private val sizeView: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(game: GameInfo) {
            titleView.text = game.name
            sizeView.text = "${game.size / 1024 / 1024} MB"
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
