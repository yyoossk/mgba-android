package com.mgba.emulator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

/**
 * Game loader activity - load and display ROM files
 */
class GameLoaderActivity : Activity() {
    companion object {
        private const val TAG = "GameLoaderActivity"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var gameAdapter: GameListAdapter
    private val gameList = mutableListOf<GameInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "GameLoaderActivity.onCreate() called")

        try {
            // Create main container
            val mainContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Create button
            val btnSelectFile = Button(this).apply {
                text = "Select Game ROM"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    Log.i(TAG, "Select game button clicked")
                    Toast.makeText(this@GameLoaderActivity, "ROM selection coming soon", Toast.LENGTH_SHORT).show()
                }
            }

            // Create RecyclerView
            recyclerView = RecyclerView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
                layoutManager = LinearLayoutManager(this@GameLoaderActivity)
            }

            // Setup adapter
            gameAdapter = GameListAdapter(gameList) { gameInfo ->
                launchGame(gameInfo.path)
            }
            recyclerView.adapter = gameAdapter

            // Add views to container
            mainContainer.addView(btnSelectFile)
            mainContainer.addView(recyclerView)

            setContentView(mainContainer)

            // Load game list
            loadGameList()

            Log.i(TAG, "GameLoaderActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in GameLoaderActivity: ${e.message}", e)
            e.printStackTrace()

            // Fallback UI
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

            gameAdapter.notifyDataSetChanged()
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
 * Game info data class
 */
data class GameInfo(
    val name: String,
    val path: String,
    val size: Long
)

/**
 * RecyclerView adapter for game list
 */
class GameListAdapter(
    private val games: List<GameInfo>,
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<GameListAdapter.ViewHolder>() {

    inner class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 16, 16, 16)

                val titleView = TextView(context).apply {
                    textSize = 18f
                }
                addView(titleView)

                val sizeView = TextView(context).apply {
                    textSize = 14f
                }
                addView(sizeView)

                setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position >= 0) {
                        onGameClick(games[position])
                    }
                }

                tag = Pair(titleView, sizeView)
            }
        ) {

        fun bind(game: GameInfo) {
            val (titleView, sizeView) = itemView.tag as Pair<TextView, TextView>
            titleView.text = game.name
            sizeView.text = "${game.size / 1024 / 1024} MB"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size
}

/**
 * Toast wrapper for Activity context
 */
object Toast {
    fun makeText(context: Activity, text: String, duration: Int): android.widget.Toast {
        return android.widget.Toast.makeText(context, text, duration)
            .also { it.show() }
    }
}
