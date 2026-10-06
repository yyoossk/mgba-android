# mGBA Android - プロジェクト構成ガイド

完成したプロジェクトスターターキットの構成と配置ガイド。

## 実装済みファイル一覧

```
mgba-android/
├── README.md                          # プロジェクト概要・使用方法
├── SETUP.md                           # 開発環境セットアップガイド
├── IMPLEMENTATION_CHECKLIST.md        # 実装進捗チェックリスト
├── PROJECT_STRUCTURE.md               # このファイル
│
├── build.gradle.kts                   # ルートレベルGradleビルド設定
├── settings.gradle.kts                # Gradleプロジェクト設定
│
├── app-build.gradle.kts               # app/build.gradle.ktsの内容
│                                       # ファイル配置時に app/build.gradle.kts に移動してください
│
├── app/src/main/
│   ├── java/com/mgba/emulator/
│   │   ├── MainActivity.kt            # メインエミュレータ画面
│   │   ├── GameLoaderActivity.kt      # ROM選択・ローダー
│   │   ├── EmulatorCore.kt            # ネイティブコアのJavaラッパー
│   │   └── SaveDataManager.kt         # セーブデータ管理（スケルトン）
│   │
│   ├── jni/
│   │   ├── CMakeLists.txt             # ネイティブビルド設定
│   │   ├── mgba_jni.c                 # JNI実装
│   │   ├── emulator_bridge.c          # エミュレーターブリッジ（スケルトン）
│   │   ├── audio_callback.c           # オーディオコールバック（スケルトン）
│   │   └── mgba/                      # mGBAコアソース（別途ダウンロード）
│   │       ├── src/core/
│   │       ├── include/
│   │       └── ...
│   │
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml           # メイン画面レイアウト（作成予定）
│   │   │   ├── activity_game_loader.xml    # ローダー画面レイアウト（作成予定）
│   │   │   └── item_game.xml               # ゲームアイテムレイアウト（作成予定）
│   │   │
│   │   ├── values/
│   │   │   ├── strings.xml            # 文字列リソース（作成予定）
│   │   │   ├── colors.xml             # カラーリソース（作成予定）
│   │   │   ├── dimens.xml             # サイズリソース（作成予定）
│   │   │   └── themes.xml             # テーマ設定（作成予定）
│   │   │
│   │   ├── drawable/
│   │   │   ├── ic_launcher.png        # ランチャーアイコン（作成予定）
│   │   │   ├── ic_play.png            # 再生ボタン（作成予定）
│   │   │   └── ic_pause.png           # 一時停止ボタン（作成予定）
│   │   │
│   │   └── xml/
│   │       └── file_paths.xml         # FileProvider設定（作成予定）
│   │
│   └── AndroidManifest.xml            # アプリマニフェスト

├── proguard-rules.pro                 # ProGuard/R8設定（作成予定）
└── gradle.properties                  # Gradle設定（作成予定）
```

## ファイル配置手順

### 1. ディレクトリ構造の作成

```bash
cd mgba-android

# ディレクトリツリーを作成
mkdir -p app/src/main/java/com/mgba/emulator
mkdir -p app/src/main/jni/mgba/src/core
mkdir -p app/src/main/jni/mgba/include
mkdir -p app/src/main/res/{layout,values,drawable,xml}
mkdir -p app/src/test
mkdir -p app/src/androidTest
```

### 2. ファイルを正しい場所に配置

```bash
# Kotlin/Java ファイル
mv EmulatorCore.kt app/src/main/java/com/mgba/emulator/
mv MainActivity.kt app/src/main/java/com/mgba/emulator/
mv GameLoaderActivity.kt app/src/main/java/com/mgba/emulator/

# JNI/C ファイル
mv mgba_jni.c app/src/main/jni/
mv CMakeLists.txt app/src/main/jni/

# Gradle設定
mv app-build.gradle.kts app/build.gradle.kts

# AndroidManifest
mv AndroidManifest.xml app/src/main/
```

### 3. mGBAコアソースの統合

```bash
# mGBAリポジトリから必要なファイルをコピー
# 参照: https://github.com/mgba-emu/mgba

cp -r /path/to/mgba/src/core/* app/src/main/jni/mgba/src/core/
cp -r /path/to/mgba/include/* app/src/main/jni/mgba/include/
```

### 4. リソースファイルの作成

各XMLレイアウトとリソースファイルは別途作成が必要です。
`SETUP.md` の「リソース作成」セクションを参照してください。

## ファイルの役割説明

### ビルド設定ファイル

| ファイル | 役割 |
|---------|------|
| `build.gradle.kts` | プラグイン、リポジトリ、共通設定 |
| `settings.gradle.kts` | プロジェクト名、モジュール定義 |
| `app/build.gradle.kts` | Android固有の設定、依存関係 |

### Kotlinコード

| ファイル | 説明 |
|---------|------|
| `EmulatorCore.kt` | JNI インターフェース、ネイティブメソッド定義 |
| `MainActivity.kt` | メイン画面、ゲームプレイUI、入力処理 |
| `GameLoaderActivity.kt` | ROM検出・選択、ゲーム起動 |
| `SaveDataManager.kt` | セーブスロット・ファイル管理（要実装） |

### ネイティブコード

| ファイル | 説明 |
|---------|------|
| `mgba_jni.c` | JNI実装、JavaとCの連携、フレームバッファ処理 |
| `CMakeLists.txt` | mGBAコアのクロスコンパイル設定 |

### ドキュメント

| ファイル | 内容 |
|---------|------|
| `README.md` | プロジェクト概要、セットアップ手順 |
| `SETUP.md` | 詳細なセットアップガイド |
| `IMPLEMENTATION_CHECKLIST.md` | 実装フェーズとチェックリスト |

## 次のステップ

### 即座に実施

1. **環境セットアップ** (`SETUP.md`)
   - JDK, Android SDK, NDK, CMakeのインストール
   - 環境変数の設定

2. **ファイル配置**
   - 上記の「ファイル配置手順」に従う
   - ディレクトリ構造を整える

3. **mGBAソース統合**
   - GitHub からmGBAをクローン
   - コアファイルを `app/src/main/jni/mgba/` にコピー
   - CMakeLists.txt を更新

### 短期開発（1-2週間）

4. **ビルド・テスト**
   - `./gradlew build` でビルド
   - エラーを修正
   - デバイス/エミュレータでテスト

5. **リソース作成**
   - XMLレイアウト作成
   - ドローアブルアイコン作成
   - 文字列・カラー定義

### 中期開発（2-4週間）

6. **機能実装**
   - フレーム描画（OpenGL ES）
   - オーディオ出力（OpenSL ES）
   - セーブ/ロード機能
   - 入力マッピング

7. **テスト・最適化**
   - ユニットテスト
   - デバイステスト
   - パフォーマンス最適化

### 長期（1-2ヶ月）

8. **ポーランド・配布**
   - ドキュメント完成
   - エラーハンドリング
   - UI/UX改善
   - Google Play Store準備

## トラブルシューティング

### ビルドエラー

```bash
# キャッシュクリア
./gradlew clean

# 詳細ログを出力してビルド
./gradlew build --stacktrace --info
```

### NDK/CMakepath関連エラー

```bash
# ANDROID_SDK_ROOT と ANDROID_NDK_ROOT を確認
echo $ANDROID_SDK_ROOT
echo $ANDROID_NDK_ROOT

# local.properties に明示的に設定
echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
echo "ndk.dir=$ANDROID_NDK_ROOT" >> local.properties
```

### mGBAソース統合エラー

```bash
# CMakeLists.txt のソースファイルパスを確認
cat app/src/main/jni/CMakeLists.txt

# ファイルが実在するか確認
ls -la app/src/main/jni/mgba/src/core/
```

## 依存ライブラリ

### Android Framework
- `androidx.appcompat:appcompat`
- `androidx.constraintlayout:constraintlayout`
- `androidx.lifecycle:lifecycle-*`
- `org.jetbrains.kotlinx:kotlinx-coroutines-*`

### ネイティブライブラリ
- `libc.so` (Cランタイム)
- `libm.so` (Math)
- `liblog.so` (Android Logging)
- `libandroid.so` (Android Native APIs)
- `libGLESv2.so` (OpenGL ES 2.0)
- `libOpenSLES.so` (OpenSL ES)

## ライセンス

- **mGBA** : MPL 2.0
- **このプロジェクト** : MPL 2.0

詳細は [LICENSE](LICENSE) を参照。

---

**質問・サポート**

問題が発生した場合は以下の順序でトラブルシューティングしてください：

1. SETUP.md の確認
2. ビルドログの詳細確認 (`--stacktrace`)
3. GitHub Issues での報告
