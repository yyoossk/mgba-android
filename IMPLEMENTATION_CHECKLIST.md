# mGBA Android - 実装チェックリスト

開発の進捗管理用チェックリスト。タスク完了時に `[x]` にチェック。

## フェーズ1: 開発環境セットアップ

- [ ] JDK 17のインストール・確認
- [ ] Android SDK (API 34)のインストール
- [ ] Android NDK (r25.1以上)のインストール
- [ ] CMake 3.22.1以上のインストール
- [ ] Gradle 8.0以上のインストール
- [ ] 環境変数の設定確認
- [ ] Android Studioプロジェクトのインポート
- [ ] Gradleビルドの動作確認

## フェーズ2: プロジェクト構造・ビルド設定

- [ ] ディレクトリ構造の作成
- [ ] build.gradle.kts (ルート)の作成
- [ ] build.gradle.kts (app)の作成
- [ ] settings.gradle.ktsの作成
- [ ] CMakeLists.txtの作成
- [ ] AndroidManifest.xmlの完成
- [ ] リソースファイル（values, layouts）の作成
- [ ] ビルドエラーの解消

## フェーズ3: JNI・ネイティブコア

- [ ] mGBAソースコードの取得
- [ ] mgba_jni.cの基本実装
- [ ] JNIメソッドシグネチャの確認
  - [ ] nativeInitialize()
  - [ ] nativeLoadROM()
  - [ ] nativeStart()
  - [ ] nativePause()
  - [ ] nativeResume()
  - [ ] nativeStop()
  - [ ] nativeSetKeyState()
  - [ ] nativeGetFrameBuffer()
  - [ ] nativeSaveState()
  - [ ] nativeLoadState()
  - [ ] nativeDestroy()
- [ ] mGBAコアのヘッダーファイル統合
- [ ] CMakeLists.txtのソースファイル設定
- [ ] クロスコンパイル環境の確認
- [ ] ネイティブライブラリのビルド・リンク

## フェーズ4: Javaラッパー・コア機能

### EmulatorCore.kt

- [ ] ネイティブライブラリのロード
- [ ] initialize()の実装
- [ ] loadROM()の実装
- [ ] start()/pause()/resume()/stop()の実装
- [ ] setKeyState()の実装
- [ ] getFrameBufferInfo()の実装
- [ ] saveState()/loadState()の実装
- [ ] destroy()の実装
- [ ] エラーハンドリング
- [ ] コルーチン対応

### MainActivity.kt

- [ ] UIレイアウト参照
- [ ] SurfaceViewの初期化
- [ ] GameLoaderActivityからのIntentデータ受信
- [ ] ROM読み込み・起動処理
- [ ] フレーム描画ループ
- [ ] キー入力マッピング
  - [ ] Zキー → A
  - [ ] Xキー → B
  - [ ] ←↑↓→ → 方向キー
  - [ ] Shift → SELECT
  - [ ] Enter → START
  - [ ] Q/W → L/R
- [ ] ポーズボタン
- [ ] セーブ/ロードボタン
- [ ] ライフサイクル管理（pause/resume/destroy）

### GameLoaderActivity.kt

- [ ] ゲーム一覧RecyclerView
- [ ] ファイルピッカー統合
- [ ] ROM検出・表示
- [ ] ゲーム起動（MainActivity遷移）
- [ ] 権限処理（READ_EXTERNAL_STORAGE等）
- [ ] ファイルコピー機能

## フェーズ5: UI・リソース

- [ ] activity_main.xmlレイアウト
  - [ ] SurfaceView
  - [ ] コントロールボタン
  - [ ] 状態表示テキスト
- [ ] activity_game_loader.xmlレイアウト
  - [ ] RecyclerView
  - [ ] ゲームリスト表示
  - [ ] ファイル選択ボタン
- [ ] item_game.xmlアイテムレイアウト
- [ ] strings.xmlリソース
- [ ] colors.xmlカラー定義
- [ ] dimens.xmlサイズ定義
- [ ] drawables/アイコン作成
  - [ ] ランチャーアイコン
  - [ ] ボタンアイコン

## フェーズ6: 画面出力・レンダリング

- [ ] OpenGL ES 2.0の統合
- [ ] フレームバッファのBitmap変換
- [ ] 画面スケーリング（デバイスサイズに合わせる）
- [ ] フレームレート制御（60 FPS）
- [ ] 描画パフォーマンス最適化
- [ ] スレッドセーフティ確保

## フェーズ7: オーディオ出力

- [ ] OpenSL ES統合
- [ ] オーディオバッファ管理
- [ ] サンプリングレート設定（32kHz）
- [ ] ステレオ出力
- [ ] オーディオコールバック実装
- [ ] 遅延最小化

## フェーズ8: セーブデータ・ストレージ

### SaveDataManager.kt

- [ ] セーブスロット管理
- [ ] セーブファイルの読み書き
- [ ] 保存先パス設定
- [ ] セーブバックアップ機能
- [ ] クラウド同期対応設計（オプション）

- [ ] getExternalFilesDir("saves")の使用
- [ ] セーブファイル形式の定義
- [ ] セーブ一覧表示機能
- [ ] セーブ削除機能

## フェーズ9: 入力・コントローラー

- [ ] キーボード入力マッピング
- [ ] 物理ゲームパッドサポート
- [ ] タッチスクリーン入力（オプション）
- [ ] キー長押し判定
- [ ] 同時押し対応

## フェーズ10: テスト・デバッグ

### ユニットテスト

- [ ] EmulatorCoreテスト
- [ ] SaveDataManagerテスト
- [ ] キー入力テスト

### インテグレーションテスト

- [ ] ROM読み込みテスト
- [ ] ゲーム起動テスト
- [ ] セーブ/ロードテスト
- [ ] キー入力テスト
- [ ] 画面出力テスト

### デバイステスト

- [ ] Pixel 6a（ARM64）
- [ ] Pixel 4a（ARM64）
- [ ] OnePlus 9（ARM64）
- [ ] 画面サイズ検証（5.5〜6.5インチ）
- [ ] OS バージョン検証
  - [ ] Android 7 (API 24)
  - [ ] Android 12 (API 31)
  - [ ] Android 14 (API 34)

### パフォーマンステスト

- [ ] FPS測定（60 FPS達成確認）
- [ ] CPU使用率測定
- [ ] メモリ使用率測定
- [ ] バッテリー消費測定
- [ ] 熱発生測定（長時間プレイ）

## フェーズ11: 最適化・調整

- [ ] ARM NEON命令対応（ARM版）
- [ ] x86最適化（x86_64版）
- [ ] メモリ使用量削減
- [ ] フレームバッファ最適化
- [ ] キャッシュ効率改善
- [ ] ビルドサイズ縮小
  - [ ] ProGuard/R8設定
  - [ ] 不要なリソース削除

## フェーズ12: ドキュメント・配布

- [ ] README.mdの完成
- [ ] SETUP.mdの完成
- [ ] ビルド手順書
- [ ] ユーザーマニュアル作成
- [ ] ライセンス表記確認（MPL 2.0）
- [ ] GitHub Releaseページ作成
- [ ] Google Play Store対応設定（オプション）

## 追加機能（オプション）

- [ ] 画面回転対応
- [ ] オンスクリーンコントローラー
- [ ] チートコード対応
- [ ] リプレイ機能
- [ ] スクリーンショット機能
- [ ] 高速フォワード（x2.0倍）
- [ ] スローモーション（x0.5倍）
- [ ] セーブスロット複数対応
- [ ] キーコンフィグカスタマイズ
- [ ] ROMメタデータ管理
- [ ] オンラインセーブ同期

## 既知の問題・メモ

- [ ] 音声出力の遅延問題（要調査）
- [ ] 一部ゲームでグラフィック乱れ（要デバッグ）
- [ ] 大型ROMのメモリ管理（要最適化）
- [ ] Bluetooth コントローラー接続問題（要検証）

---

**最終チェック**

- [ ] すべてのテストに合格
- [ ] ドキュメント完成
- [ ] ライセンス確認
- [ ] リリース準備完了
