// app/src/main/java/com/mgba/emulator/GameLoaderActivity.kt

package com.mgba.emulator

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

/**
 * ROM選択・ゲームローダー画面
 */
class GameLoaderActivity : AppCompatActivity() {

    private lateinit var gameListRecycler: RecyclerView
    private lateinit var btnSelectFile: Button
    private lateinit var gameAdapter: GameListAdapter
    private val gameList = mutableListOf<GameInfo>()

    // ファイルピッカーのコントラクト
    private val filePickerContract = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handleROMSelected(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_loader)

        // UI初期化
        initializeUI()

        // ゲームリスト読み込み
        loadGameList()
    }

    private fun initializeUI() {
        btnSelectFile = findViewById(R.id.btn_select_file)
        gameListRecycler = findViewById(R.id.game_list)

        // ゲーム一覧アダプター
        gameAdapter = GameListAdapter(gameList) { gameInfo ->
            launchGame(gameInfo.path)
        }
        gameListRecycler.apply {
            layoutManager = LinearLayoutManager(this@GameLoaderActivity)
            adapter = gameAdapter
        }

        // ファイル選択ボタン
        btnSelectFile.setOnClickListener {
            // GBAファイルタイプのみフィルター
            filePickerContract.launch("application/octet-stream")
        }
    }

    private fun loadGameList() {
        // ROMディレクトリをスキャン
        val romDir = getExternalFilesDir("roms")
        romDir?.mkdirs()

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
    }

    private fun handleROMSelected(uri: Uri) {
        try {
            val fileName = getFileName(uri)
            if (fileName != null) {
                // ファイルをコピー
                val romDir = getExternalFilesDir("roms")
                romDir?.mkdirs()
                val destFile = File(romDir, fileName)

                contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Toast.makeText(this, "ROM imported: $fileName", Toast.LENGTH_SHORT).show()
                gameList.clear()
                loadGameList()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error importing ROM: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String? {
        return if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && nameIndex >= 0) {
                    it.getString(nameIndex)
                } else {
                    null
                }
            }
        } else {
            uri.path?.substringAfterLast("/")
        }
    }

    private fun launchGame(romPath: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("ROM_PATH", romPath)
        }
        startActivity(intent)
    }
}

/**
 * ゲーム情報
 */
data class GameInfo(
    val name: String,
    val path: String,
    val size: Long
)

/**
 * ゲームリスト用アダプター
 */
class GameListAdapter(
    private val games: List<GameInfo>,
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<GameListAdapter.ViewHolder>() {

    inner class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_game, parent, false)
        ) {

        private val titleView: TextView = itemView.findViewById(R.id.game_title)
        private val sizeView: TextView = itemView.findViewById(R.id.game_size)

        fun bind(game: GameInfo) {
            titleView.text = game.name
            sizeView.text = "${game.size / 1024 / 1024} MB"
            itemView.setOnClickListener {
                onGameClick(game)
            }
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
